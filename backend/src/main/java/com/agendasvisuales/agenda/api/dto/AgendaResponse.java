package com.agendasvisuales.agenda.api.dto;

import com.agendasvisuales.agenda.domain.TipoAgendaEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AgendaResponse(
        Long id,
        String titulo,
        TipoAgendaEnum tipoAgenda,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion,
        List<DiaAgendaResponse> dias
) {
}