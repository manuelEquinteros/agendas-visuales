package com.agendasvisuales.ai.model;

import com.agendasvisuales.agenda.api.dto.AgendaRequest;

import jakarta.validation.constraints.NotBlank;

public record InterpretAgendaRequest(

		@NotBlank(message = "El mensaje es obligatorio.") String message,

		String previousUserMessage,

		String previousAssistantMessage,

		AgendaRequest context

) {

	/*
	 * Conservamos este constructor para no romper código o tests que todavía creen
	 * el request únicamente con message + context.
	 */
	public InterpretAgendaRequest(String message, AgendaRequest context) {
		this(message, null, null, context);
	}
}