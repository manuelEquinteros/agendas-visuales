package com.agendasvisuales.agenda.api.dto;

import com.agendasvisuales.agenda.domain.TipoAgendaEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record AgendaRequest(

		@NotBlank @Size(max = 150) String titulo,

		@NotNull TipoAgendaEnum tipoAgenda,

		@NotNull LocalDate fechaInicio,

		LocalDate fechaFin,

		@NotNull List<@Valid DiaAgendaRequest> dias) {
}