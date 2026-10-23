package com.agendasvisuales.ai.model;

import java.util.List;

public record AiDayDraft(

        String fecha,

        Integer orden,

        List<AiActivityDraft> actividades

) {
}