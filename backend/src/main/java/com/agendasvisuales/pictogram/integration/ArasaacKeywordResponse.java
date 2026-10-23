package com.agendasvisuales.pictogram.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArasaacKeywordResponse(

		String keyword

) {
}