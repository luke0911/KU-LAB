package com.fifthdimension.digital_twin.area.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventAreaRepository extends JpaRepository<EventArea, Long> {

    @EntityGraph(attributePaths = "eventTriggers")
    Page<EventArea> findAllByMapId(Integer mapId, Pageable pageable);
    @EntityGraph(attributePaths = "eventTriggers")
    Page<EventArea> findAllByMapIdAndNameContaining(Integer mapId, String name, Pageable pageable);
    @EntityGraph(attributePaths = "eventTriggers")
    List<EventArea> findAllByMapIdAndFloorGreaterThanEqualAndFloorLessThan(Integer mapId, float floorStart, float floorEnd);
}
