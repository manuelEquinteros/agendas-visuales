package com.agendasvisuales.ai.model;

import java.util.List;

import com.agendasvisuales.agenda.api.dto.AgendaRequest;

public record InterpretationResult(

        InterpretationStatusEnum status,

        String assistantMessage,

        List<MissingField> missingFields,

        AgendaRequest agenda

) {
}