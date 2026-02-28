package com.fifthdimension.digital_twin.map.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapRepository extends JpaRepository<Map, Integer> {
    Page<Map> findAllByNameContaining(String name, Pageable pageable);
}
