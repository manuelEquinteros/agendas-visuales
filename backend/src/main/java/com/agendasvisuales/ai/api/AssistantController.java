package com.agendasvisuales.ai.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.agendasvisuales.ai.application.AgendaInterpreter;
import com.agendasvisuales.ai.model.InterpretAgendaRequest;
import com.agendasvisuales.ai.model.InterpretationResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

	private final AgendaInterpreter agendaInterpreter;

	public AssistantController(AgendaInterpreter agendaInterpreter) {
		this.agendaInterpreter = agendaInterpreter;
	}

	@PostMapping("/interpret")
	@ResponseStatus(HttpStatus.OK)
	public InterpretationResult interpret(@Valid @RequestBody InterpretAgendaRequest request) {

		return agendaInterpreter.interpret(request);
	}
}