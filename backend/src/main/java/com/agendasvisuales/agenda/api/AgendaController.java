package com.agendasvisuales.agenda.api;

import com.agendasvisuales.agenda.api.dto.AgendaRequest;
import com.agendasvisuales.agenda.api.dto.AgendaResponse;
import com.agendasvisuales.agenda.application.AgendaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequestMapping("/api/agendas")
public class AgendaController {

	private final AgendaService agendaService;

	public AgendaController(AgendaService agendaService) {
		this.agendaService = agendaService;
	}

	@GetMapping
	public ResponseEntity<List<AgendaResponse>> list() {
		return ResponseEntity.ok(agendaService.list());
	}

	@GetMapping("/{id}")
	public ResponseEntity<AgendaResponse> get(@PathVariable Long id) {

		return ResponseEntity.ok(agendaService.get(id));
	}

	@PostMapping
	public ResponseEntity<AgendaResponse> create(@Valid @RequestBody AgendaRequest request) {

		AgendaResponse response = agendaService.create(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<AgendaResponse> update(@PathVariable Long id, @Valid @RequestBody AgendaRequest request) {

		return ResponseEntity.ok(agendaService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {

		agendaService.delete(id);

		return ResponseEntity.noContent().build();
	}
}