package com.agendasvisuales.agenda.application;

import com.agendasvisuales.agenda.api.dto.ActividadAgendaRequest;
import com.agendasvisuales.agenda.api.dto.ActividadAgendaResponse;
import com.agendasvisuales.agenda.api.dto.AgendaRequest;
import com.agendasvisuales.agenda.api.dto.AgendaResponse;
import com.agendasvisuales.agenda.api.dto.DiaAgendaRequest;
import com.agendasvisuales.agenda.api.dto.DiaAgendaResponse;
import com.agendasvisuales.agenda.api.dto.PictogramaRequest;
import com.agendasvisuales.agenda.api.dto.PictogramaResponse;
import com.agendasvisuales.agenda.domain.ActividadAgenda;
import com.agendasvisuales.agenda.domain.AgendaVisual;
import com.agendasvisuales.agenda.domain.DiaAgenda;
import com.agendasvisuales.agenda.domain.Pictograma;
import org.springframework.stereotype.Component;

@Component
public class AgendaMapper {

    public AgendaVisual toEntity(AgendaRequest request) {
        AgendaVisual agenda = new AgendaVisual(
                request.titulo(),
                request.tipoAgenda(),
                request.fechaInicio(),
                request.fechaFin()
        );

        request.dias()
                .forEach(diaRequest -> agenda.addDia(toDiaEntity(diaRequest)));

        return agenda;
    }

    public void updateEntity(AgendaVisual agenda, AgendaRequest request) {
        agenda.setTitulo(request.titulo());
        agenda.setTipoAgenda(request.tipoAgenda());
        agenda.setFechaInicio(request.fechaInicio());
        agenda.setFechaFin(request.fechaFin());

        agenda.getDias().clear();

        request.dias()
                .forEach(diaRequest -> agenda.addDia(toDiaEntity(diaRequest)));
    }

    public AgendaResponse toResponse(AgendaVisual agenda) {
        return new AgendaResponse(
                agenda.getId(),
                agenda.getTitulo(),
                agenda.getTipoAgenda(),
                agenda.getFechaInicio(),
                agenda.getFechaFin(),
                agenda.getFechaCreacion(),
                agenda.getFechaModificacion(),
                agenda.getDias()
                        .stream()
                        .map(this::toDiaResponse)
                        .toList()
        );
    }

    private DiaAgenda toDiaEntity(DiaAgendaRequest request) {
        DiaAgenda dia = new DiaAgenda(
                request.fecha(),
                request.orden()
        );

        request.actividades()
                .forEach(actividadRequest ->
                        dia.addActividad(toActividadEntity(actividadRequest))
                );

        return dia;
    }

    private ActividadAgenda toActividadEntity(ActividadAgendaRequest request) {
        ActividadAgenda actividad = new ActividadAgenda(
                request.titulo(),
                request.descripcion(),
                request.orden(),
                request.horaInicio(),
                request.horaFin(),
                request.acompaniante(),
                request.conceptoVisual()
        );

        if (request.pictograma() != null) {
            actividad.setPictograma(
                    toPictogramaEntity(request.pictograma())
            );
        }

        return actividad;
    }

    private Pictograma toPictogramaEntity(PictogramaRequest request) {
        return new Pictograma(
                request.idArasaac(),
                request.descripcion(),
                request.urlImagen()
        );
    }

    private DiaAgendaResponse toDiaResponse(DiaAgenda dia) {
        return new DiaAgendaResponse(
                dia.getId(),
                dia.getFecha(),
                dia.getOrden(),
                dia.getActividades()
                        .stream()
                        .map(this::toActividadResponse)
                        .toList()
        );
    }

    private ActividadAgendaResponse toActividadResponse(
            ActividadAgenda actividad) {

        return new ActividadAgendaResponse(
                actividad.getId(),
                actividad.getTitulo(),
                actividad.getDescripcion(),
                actividad.getOrden(),
                actividad.getHoraInicio(),
                actividad.getHoraFin(),
                actividad.getAcompaniante(),
                actividad.getConceptoVisual(),
                toPictogramaResponse(actividad.getPictograma())
        );
    }

    private PictogramaResponse toPictogramaResponse(
            Pictograma pictograma) {

        if (pictograma == null) {
            return null;
        }

        return new PictogramaResponse(
                pictograma.getIdArasaac(),
                pictograma.getDescripcion(),
                pictograma.getUrlImagen()
        );
    }
}