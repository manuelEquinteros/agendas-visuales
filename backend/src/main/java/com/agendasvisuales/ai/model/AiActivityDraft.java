package com.agendasvisuales.ai.model;

public record AiActivityDraft(

        String titulo,

        String descripcion,

        Integer orden,

        String horaInicio,

        String horaFin,

        String acompaniante

) {
}