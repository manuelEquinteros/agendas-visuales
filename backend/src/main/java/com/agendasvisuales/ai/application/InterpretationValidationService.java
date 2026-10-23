package com.agendasvisuales.ai.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.agendasvisuales.agenda.domain.TipoAgendaEnum;
import com.agendasvisuales.ai.model.AiActivityDraft;
import com.agendasvisuales.ai.model.AiAgendaDraft;
import com.agendasvisuales.ai.model.AiDayDraft;
import com.agendasvisuales.ai.model.AiInterpretationResponse;
import com.agendasvisuales.ai.model.AiMissingField;
import com.agendasvisuales.ai.model.InterpretationStatusEnum;
import com.agendasvisuales.shared.error.AiInvalidResponseException;

@Service
public class InterpretationValidationService {

	private static final Logger log = LoggerFactory.getLogger(InterpretationValidationService.class);

	public AiInterpretationResponse validateAndNormalize(AiInterpretationResponse response) {

		log.debug("""
				[AI_DIAG][VALIDATION][START]
				status={}
				agendaPresente={}
				missingFields={}
				""", response != null ? response.status() : null, response != null && response.agenda() != null,
				response != null && response.missingFields() != null ? response.missingFields().size() : null);

		if (response == null) {

			throw invalid("La IA no devolvió una respuesta.");
		}

		if (response.status() == null) {

			throw invalid("La respuesta de IA no contiene status.");
		}

		AiInterpretationResponse result = switch (response.status()) {

		case COMPLETE -> normalizeComplete(response);

		case INCOMPLETE -> normalizeIncomplete(response);

		case OUT_OF_SCOPE -> normalizeOutOfScope(response);
		};

		log.debug("""
				[AI_DIAG][VALIDATION][OK]
				status={}
				agendaPresente={}
				""", result.status(), result.agenda() != null);

		return result;
	}

	private AiInterpretationResponse normalizeComplete(AiInterpretationResponse response) {

		AiAgendaDraft agenda = normalizeAgenda(response.agenda());

		validateCompleteAgenda(agenda);

		String assistantMessage = normalizeNullableText(response.assistantMessage());

		if (assistantMessage == null) {
			assistantMessage = "Agenda interpretada correctamente.";
		}

		return new AiInterpretationResponse(InterpretationStatusEnum.COMPLETE, assistantMessage, List.of(), agenda);
	}

	private AiInterpretationResponse normalizeIncomplete(AiInterpretationResponse response) {

		List<AiMissingField> missingFields = normalizeMissingFields(response.missingFields());

		if (missingFields.isEmpty()) {

			throw invalid("Una respuesta INCOMPLETE debe indicar al menos un dato faltante.");
		}

		AiAgendaDraft agenda = response.agenda() == null ? null : normalizeAgenda(response.agenda());

		return new AiInterpretationResponse(InterpretationStatusEnum.INCOMPLETE,
				normalizeNullableText(response.assistantMessage()), missingFields, agenda);
	}

	private AiInterpretationResponse normalizeOutOfScope(AiInterpretationResponse response) {

		String assistantMessage = normalizeNullableText(response.assistantMessage());

		if (assistantMessage == null) {

			assistantMessage = "Solo puedo ayudar a crear o modificar agendas.";
		}

		return new AiInterpretationResponse(InterpretationStatusEnum.OUT_OF_SCOPE, assistantMessage, List.of(), null);
	}

	private AiAgendaDraft normalizeAgenda(AiAgendaDraft source) {

		if (source == null) {
			return null;
		}

		String titulo = normalizeNullableText(source.titulo());

		String fechaInicio = normalizeNullableText(source.fechaInicio());

		String fechaFin = normalizeNullableText(source.fechaFin());

		validateDateIfPresent(fechaInicio, "fechaInicio");

		validateDateIfPresent(fechaFin, "fechaFin");

		List<AiDayDraft> dias = source.dias() == null ? List.of() : normalizeDays(source.dias());

		/*
		 * Para una agenda diaria fechaFin no tiene significado.
		 */
		if (source.tipoAgenda() == TipoAgendaEnum.DIARIA) {

			fechaFin = null;
		}

		/*
		 * El título es opcional desde el punto de vista de interpretación.
		 *
		 * Si la IA completó la agenda pero no propuso título, usamos uno neutro y
		 * determinístico.
		 */
		if (titulo == null && source.tipoAgenda() != null) {

			titulo = source.tipoAgenda() == TipoAgendaEnum.DIARIA ? "Agenda diaria" : "Agenda semanal";
		}

		return new AiAgendaDraft(titulo, source.tipoAgenda(), fechaInicio, fechaFin, dias);
	}

	private List<AiDayDraft> normalizeDays(List<AiDayDraft> source) {

		return java.util.stream.IntStream.range(0, source.size())
				.mapToObj(index -> normalizeDay(source.get(index), index + 1)).toList();
	}

	private AiDayDraft normalizeDay(AiDayDraft source, int order) {

		if (source == null) {

			throw invalid("La respuesta contiene un día nulo.");
		}

		String fecha = normalizeNullableText(source.fecha());

		validateDateIfPresent(fecha, "dia.fecha");

		List<AiActivityDraft> actividades = source.actividades() == null ? List.of()
				: normalizeActivities(source.actividades());

		return new AiDayDraft(fecha, order, actividades);
	}

	private List<AiActivityDraft> normalizeActivities(List<AiActivityDraft> source) {

		return java.util.stream.IntStream.range(0, source.size())
				.mapToObj(index -> normalizeActivity(source.get(index), index + 1)).toList();
	}

	private AiActivityDraft normalizeActivity(AiActivityDraft source, int order) {

		if (source == null) {

			throw invalid("La respuesta contiene una actividad nula.");
		}

		String titulo = normalizeNullableText(source.titulo());

		String descripcion = normalizeNullableText(source.descripcion());

		String horaInicio = normalizeNullableText(source.horaInicio());

		String horaFin = normalizeNullableText(source.horaFin());

		String acompaniante = normalizeNullableText(source.acompaniante());

		validateTimeIfPresent(horaInicio, "horaInicio");

		validateTimeIfPresent(horaFin, "horaFin");

		return new AiActivityDraft(titulo, descripcion, order, horaInicio, horaFin, acompaniante);
	}

	private List<AiMissingField> normalizeMissingFields(List<AiMissingField> source) {

		if (source == null) {
			return List.of();
		}

		return source.stream().filter(item -> item != null)
				.map(item -> new AiMissingField(normalizeMissingFieldName(item.field()),
						normalizeNullableText(item.question())))
				.filter(item -> item.field() != null && item.question() != null).toList();
	}

	private String normalizeMissingFieldName(String value) {

		String normalized = normalizeNullableText(value);

		if (normalized == null) {
			return null;
		}

		/*
		 * Alias observado durante las pruebas iniciales con Gemini.
		 */
		if ("fecha".equalsIgnoreCase(normalized)) {
			return "fechaInicio";
		}

		return normalized;
	}

	private void validateCompleteAgenda(AiAgendaDraft agenda) {

		if (agenda == null) {

			throw invalid("Una respuesta COMPLETE debe contener una agenda.");
		}

		if (agenda.tipoAgenda() == null) {

			throw invalid("La agenda COMPLETE no contiene tipoAgenda.");
		}

		if (agenda.fechaInicio() == null) {

			throw invalid("La agenda COMPLETE no contiene fechaInicio.");
		}

		LocalDate fechaInicio = parseDate(agenda.fechaInicio(), "fechaInicio");

		if (agenda.dias() == null || agenda.dias().isEmpty()) {

			throw invalid("La agenda COMPLETE debe contener al menos un día.");
		}

		boolean hasActivities = agenda.dias().stream()
				.anyMatch(dia -> dia.actividades() != null && !dia.actividades().isEmpty());

		if (!hasActivities) {

			throw invalid("La agenda COMPLETE debe contener al menos una actividad.");
		}

		Set<LocalDate> fechasAgenda = new HashSet<>();

		for (AiDayDraft dia : agenda.dias()) {

			if (dia.fecha() == null) {

				throw invalid("Todos los días de una agenda COMPLETE deben tener fecha.");
			}

			LocalDate fechaDia = parseDate(dia.fecha(), "dia.fecha");

			if (!fechasAgenda.add(fechaDia)) {

				throw invalid("La respuesta de IA contiene dos días con la misma fecha: " + fechaDia + ".");
			}

			for (AiActivityDraft actividad : dia.actividades()) {

				if (actividad.titulo() == null) {

					throw invalid("Todas las actividades de una agenda COMPLETE deben tener título.");
				}
			}
		}

		if (agenda.tipoAgenda() == TipoAgendaEnum.DIARIA) {

			validateDailyAgenda(agenda, fechaInicio);
		}

		if (agenda.tipoAgenda() == TipoAgendaEnum.SEMANAL) {

			validateWeeklyAgenda(agenda, fechaInicio);
		}

		log.debug("""
				[AI_DIAG][VALIDATION][COMPLETE_OK]
				tipoAgenda={}
				fechaInicio={}
				fechaFin={}
				dias={}
				""", agenda.tipoAgenda(), agenda.fechaInicio(), agenda.fechaFin(), agenda.dias().size());
	}

	private void validateDailyAgenda(AiAgendaDraft agenda, LocalDate fechaInicio) {

		if (agenda.dias().size() != 1) {

			throw invalid("Una agenda DIARIA debe contener exactamente un día.");
		}

		LocalDate fechaDia = parseDate(agenda.dias().get(0).fecha(), "dia.fecha");

		if (!fechaInicio.equals(fechaDia)) {

			throw invalid("La fecha del día debe coincidir con fechaInicio en una agenda DIARIA.");
		}

		log.debug("[AI_DIAG][VALIDATION][DAILY_OK] fecha={}", fechaInicio);
	}

	private void validateWeeklyAgenda(AiAgendaDraft agenda, LocalDate fechaInicio) {

		LocalDate fechaFin = null;

		if (agenda.fechaFin() != null) {

			fechaFin = parseDate(agenda.fechaFin(), "fechaFin");

			if (fechaFin.isBefore(fechaInicio)) {

				throw invalid("fechaFin no puede ser anterior a fechaInicio.");
			}
		}

		for (AiDayDraft dia : agenda.dias()) {

			LocalDate fechaDia = parseDate(dia.fecha(), "dia.fecha");

			if (fechaDia.isBefore(fechaInicio)) {

				throw invalid("Un día de la agenda no puede ser anterior a fechaInicio.");
			}

			if (fechaFin != null && fechaDia.isAfter(fechaFin)) {

				throw invalid("Un día de la agenda no puede ser posterior a fechaFin.");
			}
		}

		log.debug("""
				[AI_DIAG][VALIDATION][WEEKLY_OK]
				fechaInicio={}
				fechaFin={}
				dias={}
				""", fechaInicio, fechaFin, agenda.dias().size());
	}

	private void validateDateIfPresent(String value, String field) {

		if (value != null) {
			parseDate(value, field);
		}
	}

	private LocalDate parseDate(String value, String field) {

		try {

			LocalDate parsed = LocalDate.parse(value);

			log.debug("[AI_DIAG][VALIDATION][DATE_OK] field={} value={}", field, value);

			return parsed;

		} catch (DateTimeParseException exception) {

			throw invalid("Fecha inválida en " + field + ": " + value);
		}
	}

	private void validateTimeIfPresent(String value, String field) {

		if (value == null) {
			return;
		}

		try {

			LocalTime.parse(value);

			log.debug("[AI_DIAG][VALIDATION][TIME_OK] field={} value={}", field, value);

		} catch (DateTimeParseException exception) {

			throw invalid("Horario inválido en " + field + ": " + value);
		}
	}

	private String normalizeNullableText(String value) {

		if (value == null) {
			return null;
		}

		String normalized = value.trim();

		if (normalized.isEmpty()) {
			return null;
		}

		/*
		 * Algunos modelos pueden devolver variantes textuales de null en campos String:
		 *
		 * null "null" :null
		 *
		 * Las tratamos como ausencia de valor.
		 */
		if (normalized.matches("(?i)^[\\s:;\"']*null[\\s:;\"']*$")) {

			return null;
		}

		return normalized;
	}

	private AiInvalidResponseException invalid(String message) {

		log.debug("[AI_DIAG][VALIDATION][ERROR] {}", message);

		return new AiInvalidResponseException(message);
	}
}