package com.fifthdimension.digital_twin.event.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisasterRepository extends JpaRepository<Disaster, Long> {
    Page<Disaster> findAllByMapId(Integer mapId, Pageable pageable);
    Page<Disaster> findAllByMapIdAndEventStatus(Integer mapId, EventStatus eventStatus, Pageable pageable);
}
