package com.agendasvisuales.agenda.api.dto;

import java.time.LocalDate;
import java.util.List;

public record DiaAgendaResponse(
        Long id,
        LocalDate fecha,
        Integer orden,
        List<ActividadAgendaResponse> actividades
) {
}