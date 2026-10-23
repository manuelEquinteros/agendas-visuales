package com.agendasvisuales.agenda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AgendaInvalidPayloadTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void shouldRejectBlankTitle() throws Exception {

		String payload = """
				{
				  "titulo": "   ",
				  "tipoAgenda": "DIARIA",
				  "fechaInicio": "2026-10-20",
				  "fechaFin": "2026-10-20",
				  "dias": [
				    {
				      "fecha": "2026-10-20",
				      "orden": 1,
				      "actividades": [
				        {
				          "titulo": "Desayunar",
				          "orden": 1,
				          "conceptoVisual": "desayunar"
				        }
				      ]
				    }
				  ]
				}
				""";

		mockMvc.perform(post("/api/agendas").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void shouldRejectInvalidAgendaType() throws Exception {

		String payload = """
				{
				  "titulo": "Agenda inválida",
				  "tipoAgenda": "MENSUAL",
				  "fechaInicio": "2026-10-20",
				  "fechaFin": "2026-10-20",
				  "dias": [
				    {
				      "fecha": "2026-10-20",
				      "orden": 1,
				      "actividades": [
				        {
				          "titulo": "Desayunar",
				          "orden": 1,
				          "conceptoVisual": "desayunar"
				        }
				      ]
				    }
				  ]
				}
				""";

		mockMvc.perform(post("/api/agendas").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void shouldRejectDailyAgendaWithMoreThanOneDay() throws Exception {

		String payload = """
				{
				  "titulo": "Agenda diaria inválida",
				  "tipoAgenda": "DIARIA",
				  "fechaInicio": "2026-10-20",
				  "fechaFin": "2026-10-20",
				  "dias": [
				    {
				      "fecha": "2026-10-20",
				      "orden": 1,
				      "actividades": [
				        {
				          "titulo": "Desayunar",
				          "orden": 1,
				          "conceptoVisual": "desayunar"
				        }
				      ]
				    },
				    {
				      "fecha": "2026-10-21",
				      "orden": 2,
				      "actividades": [
				        {
				          "titulo": "Ir a la escuela",
				          "orden": 1,
				          "conceptoVisual": "escuela"
				        }
				      ]
				    }
				  ]
				}
				""";

		mockMvc.perform(post("/api/agendas").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value("Una agenda diaria debe contener exactamente un día"));
	}

	@Test
	void shouldRejectAgendaWithoutActivities() throws Exception {

		String payload = """
				{
				  "titulo": "Agenda sin actividades",
				  "tipoAgenda": "DIARIA",
				  "fechaInicio": "2026-10-20",
				  "fechaFin": "2026-10-20",
				  "dias": [
				    {
				      "fecha": "2026-10-20",
				      "orden": 1,
				      "actividades": []
				    }
				  ]
				}
				""";

		mockMvc.perform(post("/api/agendas").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value("La agenda debe contener al menos una actividad"));
	}

	@Test
	void shouldRejectActivityWithEndTimeBeforeStartTime() throws Exception {

		String payload = """
				{
				  "titulo": "Agenda con horario inválido",
				  "tipoAgenda": "DIARIA",
				  "fechaInicio": "2026-10-20",
				  "fechaFin": "2026-10-20",
				  "dias": [
				    {
				      "fecha": "2026-10-20",
				      "orden": 1,
				      "actividades": [
				        {
				          "titulo": "Terapia",
				          "orden": 1,
				          "horaInicio": "18:00:00",
				          "horaFin": "17:00:00",
				          "conceptoVisual": "terapia"
				        }
				      ]
				    }
				  ]
				}
				""";

		mockMvc.perform(post("/api/agendas").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value("La hora fin no puede ser anterior a la hora inicio"));
	}

	@Test
	void shouldRejectDuplicateDayDates() throws Exception {

		String payload = """
				{
				  "titulo": "Agenda semanal inválida",
				  "tipoAgenda": "SEMANAL",
				  "fechaInicio": "2026-10-20",
				  "fechaFin": "2026-10-21",
				  "dias": [
				    {
				      "fecha": "2026-10-20",
				      "orden": 1,
				      "actividades": [
				        {
				          "titulo": "Desayunar",
				          "orden": 1,
				          "conceptoVisual": "desayunar"
				        }
				      ]
				    },
				    {
				      "fecha": "2026-10-20",
				      "orden": 2,
				      "actividades": [
				        {
				          "titulo": "Terapia",
				          "orden": 1,
				          "conceptoVisual": "terapia"
				        }
				      ]
				    }
				  ]
				}
				""";

		mockMvc.perform(post("/api/agendas").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value("No puede haber dos días con la misma fecha"));
	}
}