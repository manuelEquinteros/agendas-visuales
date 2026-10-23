package com.agendasvisuales.pictogram.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agendasvisuales.pictogram.api.dto.PictogramaCandidate;
import com.agendasvisuales.pictogram.application.PictogramService;

@RestController
@RequestMapping("/api/pictograms")
public class PictogramController {

	private final PictogramService pictogramService;

	public PictogramController(PictogramService pictogramService) {

		this.pictogramService = pictogramService;
	}

	@GetMapping("/search")
	public List<PictogramaCandidate> search(@RequestParam("q") String term) {

		return pictogramService.search(term);
	}
}