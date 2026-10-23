package com.agendasvisuales.ai.model;

import java.util.List;

public record AiInterpretationResponse(

        InterpretationStatusEnum status,

        String assistantMessage,

        List<AiMissingField> missingFields,

        AiAgendaDraft agenda

) {
}