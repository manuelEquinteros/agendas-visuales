package com.agendasvisuales.agenda.application;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.agendasvisuales.agenda.api.dto.ActividadAgendaRequest;
import com.agendasvisuales.agenda.api.dto.AgendaRequest;
import com.agendasvisuales.agenda.api.dto.DiaAgendaRequest;
import com.agendasvisuales.agenda.domain.TipoAgendaEnum;
import com.agendasvisuales.shared.error.AgendaValidationException;

@Component
public class AgendaValidator {

	public void validate(AgendaRequest request) {

		if (request.dias() == null || request.dias().isEmpty()) {
			throw new AgendaValidationException("La agenda debe contener al menos un día");
		}

		validateFechasAgenda(request);

		if (request.tipoAgenda() == TipoAgendaEnum.DIARIA) {
			validateAgendaDiaria(request);
		}

		if (request.tipoAgenda() == TipoAgendaEnum.SEMANAL) {
			validateAgendaSemanal(request);
		}

		validateDias(request);
		validateCantidadTotalActividades(request);
	}

	private void validateFechasAgenda(AgendaRequest request) {

		if (request.fechaFin() != null && request.fechaFin().isBefore(request.fechaInicio())) {

			throw new AgendaValidationException("La fecha fin no puede ser anterior a la fecha inicio");
		}
	}

	private void validateAgendaDiaria(AgendaRequest request) {

		if (request.dias().size() != 1) {
			throw new AgendaValidationException("Una agenda diaria debe contener exactamente un día");
		}

		DiaAgendaRequest dia = request.dias().get(0);

		if (!dia.fecha().equals(request.fechaInicio())) {
			throw new AgendaValidationException(
					"La fecha del día debe coincidir con la fecha inicio " + "de una agenda diaria");
		}

		if (request.fechaFin() != null && !request.fechaFin().equals(request.fechaInicio())) {

			throw new AgendaValidationException(
					"En una agenda diaria la fecha fin debe ser igual " + "a la fecha inicio");
		}
	}

	private void validateAgendaSemanal(AgendaRequest request) {

		if (request.dias().size() < 2) {
			throw new AgendaValidationException("Una agenda semanal debe contener más de un día");
		}

		if (request.fechaFin() == null) {
			throw new AgendaValidationException("Una agenda semanal debe tener fecha fin");
		}

		for (DiaAgendaRequest dia : request.dias()) {

			if (dia.fecha().isBefore(request.fechaInicio()) || dia.fecha().isAfter(request.fechaFin())) {

				throw new AgendaValidationException(
						"Todos los días de una agenda semanal deben estar " + "dentro del período de la agenda");
			}
		}
	}

	private void validateDias(AgendaRequest request) {

		Set<LocalDate> fechas = new HashSet<>();
		Set<Integer> ordenes = new HashSet<>();

		for (DiaAgendaRequest dia : request.dias()) {

			if (dia.orden() == null || dia.orden() <= 0) {
				throw new AgendaValidationException("El orden de un día debe ser mayor que cero");
			}

			if (!fechas.add(dia.fecha())) {
				throw new AgendaValidationException("No puede haber dos días con la misma fecha");
			}

			if (!ordenes.add(dia.orden())) {
				throw new AgendaValidationException("No puede haber dos días con el mismo orden");
			}

			validateActividades(dia);
		}
	}

	private void validateActividades(DiaAgendaRequest dia) {

		List<ActividadAgendaRequest> actividades = dia.actividades();

		if (actividades == null) {
			throw new AgendaValidationException("La lista de actividades no puede ser null");
		}

		Set<Integer> ordenes = new HashSet<>();

		for (ActividadAgendaRequest actividad : actividades) {

			if (actividad.orden() == null || actividad.orden() <= 0) {
				throw new AgendaValidationException("El orden de una actividad debe ser mayor que cero");
			}

			if (!ordenes.add(actividad.orden())) {
				throw new AgendaValidationException(
						"No puede haber dos actividades con el mismo orden " + "dentro de un día");
			}

			validateHorario(actividad);
		}
	}

	private void validateHorario(ActividadAgendaRequest actividad) {

		if (actividad.horaInicio() != null && actividad.horaFin() != null
				&& actividad.horaFin().isBefore(actividad.horaInicio())) {

			throw new AgendaValidationException("La hora fin no puede ser anterior a la hora inicio");
		}
	}

	private void validateCantidadTotalActividades(AgendaRequest request) {

		long cantidadActividades = request.dias().stream().map(DiaAgendaRequest::actividades)
				.filter(actividades -> actividades != null).mapToLong(List::size).sum();

		if (cantidadActividades == 0) {
			throw new AgendaValidationException("La agenda debe contener al menos una actividad");
		}
	}
}