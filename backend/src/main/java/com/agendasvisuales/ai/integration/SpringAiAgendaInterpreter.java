package com.agendasvisuales.ai.integration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.io.Resource;

import com.agendasvisuales.agenda.api.dto.ActividadAgendaRequest;
import com.agendasvisuales.agenda.api.dto.AgendaRequest;
import com.agendasvisuales.agenda.api.dto.DiaAgendaRequest;
import com.agendasvisuales.ai.application.AgendaInterpreter;
import com.agendasvisuales.ai.application.InterpretationValidationService;
import com.agendasvisuales.ai.model.AiActivityDraft;
import com.agendasvisuales.ai.model.AiAgendaDraft;
import com.agendasvisuales.ai.model.AiDayDraft;
import com.agendasvisuales.ai.model.AiInterpretationResponse;
import com.agendasvisuales.ai.model.InterpretAgendaRequest;
import com.agendasvisuales.ai.model.InterpretationResult;
import com.agendasvisuales.ai.model.MissingField;
import com.agendasvisuales.shared.error.AiUnavailableException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class SpringAiAgendaInterpreter implements AgendaInterpreter {

	private static final Logger log = LoggerFactory.getLogger(SpringAiAgendaInterpreter.class);

	private final ChatClient chatClient;
	private final JsonMapper jsonMapper;
	private final Resource systemPrompt;
	private final InterpretationValidationService validationService;

	public SpringAiAgendaInterpreter(ChatClient.Builder chatClientBuilder, JsonMapper jsonMapper, Resource systemPrompt,
			InterpretationValidationService validationService) {

		this.chatClient = chatClientBuilder.build();
		this.jsonMapper = jsonMapper;
		this.systemPrompt = systemPrompt;
		this.validationService = validationService;
	}

	@Override
	public InterpretationResult interpret(InterpretAgendaRequest request) {

		LocalDate currentDate = LocalDate.now();

		String currentAgendaJson = serializeCurrentAgenda(request.context());

		String userPrompt = """
				Fecha actual:
				%s

				Mensaje anterior del usuario:
				%s

				Respuesta anterior del asistente:
				%s

				Último mensaje del usuario:
				%s

				Borrador actual de la agenda:
				%s
				""".formatted(LocalDate.now(), request.previousUserMessage(), request.previousAssistantMessage(),
				request.message(), currentAgendaJson);

		if (log.isDebugEnabled()) {

			log.debug("""
					[AI_DIAG][INPUT]
					fechaActual={}
					instruccionUsuario={}
					contextoAgenda={}
					""", currentDate, request.message(), currentAgendaJson);
		}

		ResponseEntity<ChatResponse, AiInterpretationResponse> aiExchange;

		try {

			aiExchange = chatClient.prompt().system(systemPrompt).user(userPrompt).call()
					.responseEntity(AiInterpretationResponse.class, spec -> spec.useProviderStructuredOutput());

		} catch (RuntimeException exception) {

			throw new AiUnavailableException("No fue posible comunicarse con el servicio de IA.", exception);
		}

		String rawResponse = extractRawResponse(aiExchange);

		log.debug("[AI_DIAG][RAW_RESPONSE] {}", rawResponse);

		AiInterpretationResponse aiResponse = aiExchange.entity();

		log.debug("[AI_DIAG][PARSED_RESPONSE] {}", serializeForLog(aiResponse));

		AiInterpretationResponse validatedResponse = validationService.validateAndNormalize(aiResponse);

		InterpretationResult result = mapResponse(validatedResponse);

		log.debug("[AI_DIAG][FINAL_OUTPUT] {}", serializeForLog(result));

		return result;
	}

	private InterpretationResult mapResponse(AiInterpretationResponse response) {

		List<MissingField> missingFields = response.missingFields() == null ? List.of()
				: response.missingFields().stream().map(item -> new MissingField(item.field(), item.question()))
						.toList();

		AgendaRequest agenda = response.agenda() == null ? null : mapAgenda(response.agenda());

		return new InterpretationResult(response.status(), response.assistantMessage(), missingFields, agenda);
	}

	private String extractRawResponse(ResponseEntity<ChatResponse, AiInterpretationResponse> aiExchange) {

		if (aiExchange == null || aiExchange.response() == null || aiExchange.response().getResult() == null
				|| aiExchange.response().getResult().getOutput() == null) {

			return "null";
		}

		String text = aiExchange.response().getResult().getOutput().getText();

		return text != null ? text : "null";
	}

	private String serializeForLog(Object value) {

		if (value == null) {
			return "null";
		}

		try {

			return jsonMapper.writeValueAsString(value);

		} catch (JacksonException exception) {

			/*
			 * El logging de diagnóstico nunca debe modificar el comportamiento funcional.
			 */
			return "<no fue posible serializar " + value.getClass().getSimpleName() + ">";
		}
	}

	private AgendaRequest mapAgenda(AiAgendaDraft source) {

		List<DiaAgendaRequest> dias = source.dias() == null ? List.of()
				: source.dias().stream().map(this::mapDay).toList();

		return new AgendaRequest(source.titulo(), source.tipoAgenda(), parseDate(source.fechaInicio()),
				parseDate(source.fechaFin()), dias);
	}

	private DiaAgendaRequest mapDay(AiDayDraft source) {

		List<ActividadAgendaRequest> actividades = source.actividades() == null ? List.of()
				: source.actividades().stream().map(this::mapActivity).toList();

		return new DiaAgendaRequest(null, parseDate(source.fecha()), source.orden(), actividades);
	}

	private ActividadAgendaRequest mapActivity(AiActivityDraft source) {

		return new ActividadAgendaRequest(null, source.titulo(), normalizeNullableValue(source.descripcion()),
				source.orden(), parseTime(source.horaInicio()), parseTime(source.horaFin()),
				normalizeNullableValue(source.acompaniante()), createVisualConcept(source.titulo()), null);
	}

	private String serializeCurrentAgenda(AgendaRequest agenda) {

		if (agenda == null) {
			return "null";
		}

		AiAgendaDraft aiAgenda = toAiAgendaDraft(agenda);

		try {
			return jsonMapper.writeValueAsString(aiAgenda);
		} catch (JacksonException exception) {
			throw new IllegalStateException("No fue posible serializar el borrador de agenda.", exception);
		}
	}

	private AiAgendaDraft toAiAgendaDraft(AgendaRequest agenda) {

		List<AiDayDraft> dias = agenda.dias() == null ? List.of()
				: agenda.dias().stream().map(dia -> new AiDayDraft(dia.fecha() != null ? dia.fecha().toString() : null,
						dia.orden(),
						dia.actividades() == null ? List.of()
								: dia.actividades().stream()
										.map(actividad -> new AiActivityDraft(actividad.titulo(),
												actividad.descripcion(), actividad.orden(),
												actividad.horaInicio() != null ? actividad.horaInicio().toString()
														: null,
												actividad.horaFin() != null ? actividad.horaFin().toString() : null,
												actividad.acompaniante()))
										.toList()))
						.toList();

		return new AiAgendaDraft(agenda.titulo(), agenda.tipoAgenda(),
				agenda.fechaInicio() != null ? agenda.fechaInicio().toString() : null,
				agenda.fechaFin() != null ? agenda.fechaFin().toString() : null, dias);
	}

	private LocalDate parseDate(String value) {

		String normalizedValue = normalizeNullableValue(value);

		if (normalizedValue == null) {
			return null;
		}

		return LocalDate.parse(normalizedValue);
	}

	private LocalTime parseTime(String value) {

		String normalizedValue = normalizeNullableValue(value);

		if (normalizedValue == null) {
			return null;
		}

		return LocalTime.parse(normalizedValue);
	}

	private String normalizeNullableValue(String value) {

		if (value == null) {
			return null;
		}

		String normalizedValue = value.trim();

		if (normalizedValue.isEmpty() || normalizedValue.equalsIgnoreCase("null")) {

			return null;
		}

		return normalizedValue;
	}

	private String createVisualConcept(String title) {

		if (title == null || title.isBlank()) {
			return null;
		}

		return title.trim().toLowerCase(Locale.ROOT);
	}
}