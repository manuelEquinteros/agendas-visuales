package com.agendasvisuales.agenda.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PictogramaRequest(

		@NotNull Long idArasaac,

		@NotBlank @Size(max = 200) String descripcion,

		@NotBlank @Size(max = 500) String urlImagen) {
}