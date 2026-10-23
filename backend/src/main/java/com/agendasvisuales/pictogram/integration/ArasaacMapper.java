package com.agendasvisuales.pictogram.integration;

import java.util.List;

import org.springframework.stereotype.Component;

import com.agendasvisuales.pictogram.api.dto.PictogramaCandidate;

@Component
public class ArasaacMapper {

	private static final String STATIC_BASE_URL = "https://static.arasaac.org/pictograms";

	public List<PictogramaCandidate> toCandidates(List<ArasaacPictogramResponse> responses) {

		if (responses == null || responses.isEmpty()) {
			return List.of();
		}

		return responses.stream().filter(response -> response != null && response.id() != null).map(this::toCandidate)
				.toList();
	}

	public PictogramaCandidate toCandidate(ArasaacPictogramResponse response) {

		Long id = response.id();

		String descripcion = extractDescription(response);

		String urlImagen = STATIC_BASE_URL + "/" + id + "/" + id + "_500.png";

		return new PictogramaCandidate(id, descripcion, urlImagen);
	}

	private String extractDescription(ArasaacPictogramResponse response) {

		if (response.keywords() == null || response.keywords().isEmpty()) {

			return "Pictograma " + response.id();
		}

		return response.keywords().stream()
				.filter(keyword -> keyword != null && keyword.keyword() != null && !keyword.keyword().isBlank())
				.map(ArasaacKeywordResponse::keyword).findFirst().orElse("Pictograma " + response.id());
	}
}