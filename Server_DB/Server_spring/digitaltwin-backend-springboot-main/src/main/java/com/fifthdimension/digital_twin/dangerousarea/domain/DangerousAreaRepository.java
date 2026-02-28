package com.fifthdimension.digital_twin.dangerousarea.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DangerousAreaRepository extends JpaRepository<DangerousArea, Long> {
    List<DangerousArea> findAllByMapId(Integer mapId);
}
