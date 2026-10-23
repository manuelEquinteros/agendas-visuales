package com.agendasvisuales.agenda.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record ActividadAgendaRequest(

		Long id,

		@NotBlank @Size(max = 150) String titulo,

		@Size(max = 500) String descripcion,

		@NotNull Integer orden,

		LocalTime horaInicio,

		LocalTime horaFin,

		@Size(max = 150) String acompaniante,

		@NotBlank @Size(max = 150) String conceptoVisual,

		@Valid PictogramaRequest pictograma) {
}