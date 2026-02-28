package com.fifthdimension.digital_twin.event.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidentRepository extends JpaRepository<Accident, Long> {
    Page<Accident> findAllByMapId(Integer mapId, Pageable pageable);
    Page<Accident> findAllByMapIdAndEventStatus(Integer mapId, EventStatus eventStatus, Pageable pageable);
}
