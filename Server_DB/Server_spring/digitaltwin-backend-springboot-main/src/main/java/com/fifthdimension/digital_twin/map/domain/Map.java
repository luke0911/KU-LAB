package com.fifthdimension.digital_twin.map.domain;

import com.fifthdimension.digital_twin.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity(name = "maps")
@SQLRestriction("is_deleted = false")
public class Map extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Integer id;

    @Column(name = "map_name", nullable = false, length = 50)
    private String name;
}
