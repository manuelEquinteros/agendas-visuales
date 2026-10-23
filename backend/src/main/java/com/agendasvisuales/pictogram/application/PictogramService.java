package com.agendasvisuales.pictogram.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.agendasvisuales.pictogram.api.dto.PictogramaCandidate;
import com.agendasvisuales.pictogram.integration.ArasaacClient;
import com.agendasvisuales.pictogram.integration.ArasaacMapper;
import com.agendasvisuales.pictogram.integration.ArasaacPictogramResponse;

@Service
public class PictogramService {

	private final ArasaacClient arasaacClient;
	private final ArasaacMapper arasaacMapper;

	public PictogramService(ArasaacClient arasaacClient, ArasaacMapper arasaacMapper) {

		this.arasaacClient = arasaacClient;

		this.arasaacMapper = arasaacMapper;
	}

	public List<PictogramaCandidate> search(String term) {

		if (term == null || term.isBlank()) {
			return List.of();
		}

		List<ArasaacPictogramResponse> responses = arasaacClient.search(term.trim());

		return arasaacMapper.toCandidates(responses);
	}
}