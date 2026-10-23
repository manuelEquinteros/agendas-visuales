package com.agendasvisuales.shared.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.agendasvisuales.ai.application.AgendaInterpreter;
import com.agendasvisuales.ai.application.InterpretationValidationService;
import com.agendasvisuales.ai.integration.FakeAgendaInterpreter;
import com.agendasvisuales.ai.integration.SpringAiAgendaInterpreter;

import tools.jackson.databind.json.JsonMapper;

@Configuration
public class AiConfig {

	@Bean
	@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "fake", matchIfMissing = true)
	public AgendaInterpreter fakeAgendaInterpreter() {

		return new FakeAgendaInterpreter();
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "gemini")
	public AgendaInterpreter springAiAgendaInterpreter(ChatClient.Builder chatClientBuilder, JsonMapper jsonMapper,
			@Value("classpath:/prompts/create-agenda-system.txt") Resource systemPrompt,
			InterpretationValidationService validationService) {

		return new SpringAiAgendaInterpreter(chatClientBuilder, jsonMapper, systemPrompt, validationService);
	}
}