package com.agendasvisuales.agenda.persistence;

import com.agendasvisuales.agenda.domain.AgendaVisual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendaRepository extends JpaRepository<AgendaVisual, Long> {
}