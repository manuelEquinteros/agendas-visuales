package com.agendasvisuales.ai.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.agendasvisuales.agenda.domain.TipoAgendaEnum;
import com.agendasvisuales.ai.model.AiActivityDraft;
import com.agendasvisuales.ai.model.AiAgendaDraft;
import com.agendasvisuales.ai.model.AiDayDraft;
import com.agendasvisuales.ai.model.AiInterpretationResponse;
import com.agendasvisuales.ai.model.InterpretationStatusEnum;
import com.agendasvisuales.shared.error.AiInvalidResponseException;

class InterpretationValidationServiceTest {

	private final InterpretationValidationService validationService = new InterpretationValidationService();

	@Test
	void shouldRejectWeeklyAgendaWithDuplicateDates() {

		AiActivityDraft futbol = new AiActivityDraft("Fútbol", null, 1, "20:00", null, null);

		AiDayDraft miercoles = new AiDayDraft("2026-10-07", 1, List.of(futbol));

		AiDayDraft jueves = new AiDayDraft("2026-10-08", 2, List.of());

		AiDayDraft juevesDuplicado = new AiDayDraft("2026-10-08", 3, List.of(futbol));

		AiDayDraft viernes = new AiDayDraft("2026-10-09", 4, List.of());

		AiAgendaDraft agenda = new AiAgendaDraft("Agenda semanal", TipoAgendaEnum.SEMANAL, "2026-10-07", "2026-10-10",
				List.of(miercoles, jueves, juevesDuplicado, viernes));

		AiInterpretationResponse response = new AiInterpretationResponse(InterpretationStatusEnum.COMPLETE,
				"Agenda actualizada.", List.of(), agenda);

		AiInvalidResponseException exception = assertThrows(AiInvalidResponseException.class,
				() -> validationService.validateAndNormalize(response));

		assertTrue(exception.getMessage().contains("dos días con la misma fecha: 2026-10-08"));
	}
}