package com.agendasvisuales.agenda.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agendasvisuales.agenda.api.dto.AgendaRequest;
import com.agendasvisuales.agenda.api.dto.AgendaResponse;
import com.agendasvisuales.agenda.domain.ActividadAgenda;
import com.agendasvisuales.agenda.domain.AgendaVisual;
import com.agendasvisuales.agenda.domain.DiaAgenda;
import com.agendasvisuales.agenda.domain.Pictograma;
import com.agendasvisuales.agenda.persistence.AgendaRepository;
import com.agendasvisuales.agenda.persistence.PictogramaRepository;
import com.agendasvisuales.shared.error.AgendaNotFoundException;

@Service
@Transactional(readOnly = true)
public class AgendaService {

	private final AgendaRepository agendaRepository;
	private final PictogramaRepository pictogramaRepository;
	private final AgendaMapper agendaMapper;
	private final AgendaValidator agendaValidator;

	public AgendaService(AgendaRepository agendaRepository, PictogramaRepository pictogramaRepository,
			AgendaMapper agendaMapper, AgendaValidator agendaValidator) {

		this.agendaRepository = agendaRepository;
		this.pictogramaRepository = pictogramaRepository;
		this.agendaMapper = agendaMapper;
		this.agendaValidator = agendaValidator;
	}

	public List<AgendaResponse> list() {
		return agendaRepository.findAll().stream().map(agendaMapper::toResponse).toList();
	}

	public AgendaResponse get(Long id) {
		AgendaVisual agenda = findAgenda(id);

		return agendaMapper.toResponse(agenda);
	}

	@Transactional
	public AgendaResponse create(AgendaRequest request) {

		agendaValidator.validate(request);

		AgendaVisual agenda = agendaMapper.toEntity(request);

		resolvePictogramas(agenda);

		AgendaVisual agendaGuardada = agendaRepository.save(agenda);

		return agendaMapper.toResponse(agendaGuardada);
	}

	@Transactional
	public AgendaResponse update(Long id, AgendaRequest request) {

		agendaValidator.validate(request);

		AgendaVisual agenda = findAgenda(id);

		agendaMapper.updateEntity(agenda, request);

		resolvePictogramas(agenda);

		AgendaVisual agendaGuardada = agendaRepository.save(agenda);

		return agendaMapper.toResponse(agendaGuardada);
	}

	@Transactional
	public void delete(Long id) {

		AgendaVisual agenda = findAgenda(id);

		agendaRepository.delete(agenda);
	}

	private AgendaVisual findAgenda(Long id) {
		return agendaRepository.findById(id).orElseThrow(() -> new AgendaNotFoundException(id));
	}

	private void resolvePictogramas(AgendaVisual agenda) {

		for (DiaAgenda dia : agenda.getDias()) {

			for (ActividadAgenda actividad : dia.getActividades()) {

				Pictograma pictograma = actividad.getPictograma();

				if (pictograma == null) {
					continue;
				}

				Pictograma pictogramaPersistido = pictogramaRepository.findById(pictograma.getIdArasaac())
						.map(existente -> {
							existente.setDescripcion(pictograma.getDescripcion());

							existente.setUrlImagen(pictograma.getUrlImagen());

							return existente;
						}).orElseGet(() -> pictogramaRepository.save(pictograma));

				actividad.setPictograma(pictogramaPersistido);
			}
		}
	}
}