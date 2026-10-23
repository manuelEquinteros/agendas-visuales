package com.agendasvisuales.agenda;

import com.agendasvisuales.agenda.api.dto.ActividadAgendaRequest;
import com.agendasvisuales.agenda.api.dto.AgendaRequest;
import com.agendasvisuales.agenda.api.dto.AgendaResponse;
import com.agendasvisuales.agenda.api.dto.DiaAgendaRequest;
import com.agendasvisuales.agenda.api.dto.PictogramaRequest;
import com.agendasvisuales.agenda.application.AgendaService;
import com.agendasvisuales.agenda.domain.TipoAgendaEnum;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AgendaPersistenceTest {

    @Autowired
    private AgendaService agendaService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndReloadCompleteAgenda() {

        PictogramaRequest pictograma = new PictogramaRequest(
                999999001L,
                "escuela",
                "https://example.com/pictogramas/escuela.png"
        );

        ActividadAgendaRequest actividad1 =
                new ActividadAgendaRequest(
                        null,
                        "Ir a la escuela",
                        "Prepararse para comenzar la jornada escolar",
                        1,
                        LocalTime.of(8, 0),
                        LocalTime.of(8, 30),
                        "Mamá",
                        "escuela",
                        pictograma
                );

        ActividadAgendaRequest actividad2 =
                new ActividadAgendaRequest(
                        null,
                        "Almorzar",
                        null,
                        2,
                        null,
                        null,
                        null,
                        "almorzar",
                        null
                );

        DiaAgendaRequest dia = new DiaAgendaRequest(
                null,
                LocalDate.of(2026, 10, 5),
                1,
                List.of(actividad1, actividad2)
        );

        AgendaRequest request = new AgendaRequest(
                "Agenda del lunes",
                TipoAgendaEnum.DIARIA,
                LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 5),
                List.of(dia)
        );

        AgendaResponse creada =
                agendaService.create(request);

        assertThat(creada.id()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        AgendaResponse recuperada =
                agendaService.get(creada.id());

        assertThat(recuperada.id())
                .isEqualTo(creada.id());

        assertThat(recuperada.titulo())
                .isEqualTo("Agenda del lunes");

        assertThat(recuperada.tipoAgenda())
                .isEqualTo(TipoAgendaEnum.DIARIA);

        assertThat(recuperada.dias())
                .hasSize(1);

        assertThat(recuperada.dias().get(0).actividades())
                .hasSize(2);

        assertThat(
                recuperada.dias()
                        .get(0)
                        .actividades()
                        .get(0)
                        .titulo()
        ).isEqualTo("Ir a la escuela");

        assertThat(
                recuperada.dias()
                        .get(0)
                        .actividades()
                        .get(0)
                        .pictograma()
        ).isNotNull();

        assertThat(
                recuperada.dias()
                        .get(0)
                        .actividades()
                        .get(0)
                        .pictograma()
                        .idArasaac()
        ).isEqualTo(999999001L);
    }
}