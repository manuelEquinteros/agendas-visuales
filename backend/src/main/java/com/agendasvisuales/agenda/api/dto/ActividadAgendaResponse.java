package com.agendasvisuales.agenda.api.dto;

import java.time.LocalTime;

public record ActividadAgendaResponse(
        Long id,
        String titulo,
        String descripcion,
        Integer orden,
        LocalTime horaInicio,
        LocalTime horaFin,
        String acompaniante,
        String conceptoVisual,
        PictogramaResponse pictograma
) {
}