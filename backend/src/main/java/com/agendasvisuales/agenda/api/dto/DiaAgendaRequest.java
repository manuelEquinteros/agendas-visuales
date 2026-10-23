package com.agendasvisuales.agenda.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record DiaAgendaRequest(

		Long id,

		@NotNull LocalDate fecha,

		@NotNull Integer orden,

		@NotNull List<@Valid ActividadAgendaRequest> actividades) {
}