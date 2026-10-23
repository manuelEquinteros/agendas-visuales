package com.agendasvisuales.ai.integration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agendasvisuales.agenda.api.dto.ActividadAgendaRequest;
import com.agendasvisuales.agenda.api.dto.AgendaRequest;
import com.agendasvisuales.agenda.api.dto.DiaAgendaRequest;
import com.agendasvisuales.agenda.domain.TipoAgendaEnum;
import com.agendasvisuales.ai.application.AgendaInterpreter;
import com.agendasvisuales.ai.model.InterpretAgendaRequest;
import com.agendasvisuales.ai.model.InterpretationResult;
import com.agendasvisuales.ai.model.InterpretationStatusEnum;
import com.agendasvisuales.ai.model.MissingField;

public class FakeAgendaInterpreter implements AgendaInterpreter {

	private static final Pattern ISO_DATE_PATTERN = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");

	private static final Pattern LOCAL_DATE_PATTERN = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b");

	@Override
	public InterpretationResult interpret(InterpretAgendaRequest request) {

		String originalMessage = request.message().trim();

		String normalizedMessage = originalMessage.toLowerCase(Locale.ROOT);

		/*
		 * Si recibimos contexto parcial, estamos continuando una interpretación
		 * anterior.
		 */
		if (request.context() != null && request.context().fechaInicio() == null) {

			LocalDate fecha = extractDate(originalMessage);

			if (fecha == null) {

				return incompleteDateResult(request.context());
			}

			return completeDailyAgenda(fecha);
		}

		/*
		 * Escenario determinístico para probar INCOMPLETE.
		 */
		if (normalizedMessage.contains("sin fecha")) {

			return incompleteDateResult(createPartialAgenda());
		}

		/*
		 * Escenario COMPLETE normal.
		 */
		return completeDailyAgenda(LocalDate.of(2026, 10, 12));
	}

	private InterpretationResult incompleteDateResult(AgendaRequest partialAgenda) {

		MissingField missingField = new MissingField("fechaInicio", "¿Qué día querés que comience la agenda?");

		return new InterpretationResult(InterpretationStatusEnum.INCOMPLETE, missingField.question(),
				List.of(missingField), partialAgenda);
	}

	private AgendaRequest createPartialAgenda() {

		return new AgendaRequest("Rutina de mañana generada", TipoAgendaEnum.DIARIA, null, null, List.of());
	}

	private InterpretationResult completeDailyAgenda(LocalDate fecha) {

		ActividadAgendaRequest desayuno = new ActividadAgendaRequest(null, "Desayunar", "Preparar y tomar el desayuno",
				1, LocalTime.of(8, 0), LocalTime.of(8, 30), null, "desayunar", null);

		ActividadAgendaRequest vestirse = new ActividadAgendaRequest(null, "Vestirse", null, 2, LocalTime.of(8, 30),
				null, null, "vestirse", null);

		ActividadAgendaRequest escuela = new ActividadAgendaRequest(null, "Ir a la escuela", null, 3,
				LocalTime.of(9, 0), null, "Mamá", "ir a la escuela", null);

		DiaAgendaRequest dia = new DiaAgendaRequest(null, fecha, 1, List.of(desayuno, vestirse, escuela));

		AgendaRequest agenda = new AgendaRequest("Rutina de mañana generada", TipoAgendaEnum.DIARIA, fecha, null,
				List.of(dia));

		return new InterpretationResult(InterpretationStatusEnum.COMPLETE, "Preparé la agenda con la fecha indicada.",
				List.of(), agenda);
	}

	private LocalDate extractDate(String message) {

		Matcher isoMatcher = ISO_DATE_PATTERN.matcher(message);

		if (isoMatcher.find()) {

			try {

				return LocalDate.parse(isoMatcher.group(1));

			} catch (DateTimeParseException ignored) {
				return null;
			}
		}

		Matcher localMatcher = LOCAL_DATE_PATTERN.matcher(message);

		if (localMatcher.find()) {

			try {

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

				return LocalDate.parse(localMatcher.group(1), formatter);

			} catch (DateTimeParseException ignored) {
				return null;
			}
		}

		return null;
	}
}