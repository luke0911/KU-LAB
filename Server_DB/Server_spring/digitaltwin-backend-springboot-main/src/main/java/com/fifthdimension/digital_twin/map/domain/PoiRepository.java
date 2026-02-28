package com.fifthdimension.digital_twin.map.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoiRepository extends JpaRepository<Poi, Long> {
    Page<Poi> findAllByMapId(Integer mapId, Pageable pageable);

    Page<Poi> findAllByMapIdAndNameContainingIgnoreCase(Integer mapId, String keyword, Pageable pageable);

    List<Poi> findAllByMapIdAndFloorGreaterThanEqualAndFloorLessThan(Integer mapId, float floorStart, float floorEnd);
}
