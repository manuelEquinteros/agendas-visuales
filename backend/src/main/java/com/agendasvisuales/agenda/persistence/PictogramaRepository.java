package com.agendasvisuales.agenda.persistence;

import com.agendasvisuales.agenda.domain.Pictograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PictogramaRepository extends JpaRepository<Pictograma, Long> {
}