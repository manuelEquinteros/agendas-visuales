package com.agendasvisuales.ai.model;

import java.util.List;

import com.agendasvisuales.agenda.domain.TipoAgendaEnum;

public record AiAgendaDraft(

        String titulo,

        TipoAgendaEnum tipoAgenda,

        String fechaInicio,

        String fechaFin,

        List<AiDayDraft> dias

) {
}