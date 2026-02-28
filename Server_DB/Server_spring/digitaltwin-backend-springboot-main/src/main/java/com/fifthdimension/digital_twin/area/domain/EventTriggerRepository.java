package com.fifthdimension.digital_twin.area.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventTriggerRepository extends JpaRepository<EventTrigger, Long> {
    Page<EventTrigger> findAllByEventAreaId(Long eventAreaId, Pageable pageable);
    Page<EventTrigger> findAllByEventAreaIdAndTriggerNameContaining(Long eventAreaId, String name, Pageable pageable);
}
