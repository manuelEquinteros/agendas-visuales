package com.agendasvisuales.pictogram.integration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArasaacPictogramResponse(

        @JsonProperty("_id")
        Long id,

        List<ArasaacKeywordResponse> keywords

) {
}