package com.fifthdimension.digital_twin.area.domain;

import com.fifthdimension.digital_twin.global.entity.BaseEntity;
import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.infrastructure.converter.PointListConverter;
import com.fifthdimension.digital_twin.map.domain.Map;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

import static com.fifthdimension.digital_twin.global.util.PolygonUtils.normalizePolygon;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity(name = "event_areas")
@SQLRestriction("is_deleted = false")
public class EventArea extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_area_id")
    private Long id;

    @Column(name = "event_area_name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "map_id", nullable = false)
    private Map map;

    @Column(name = "event_area_floor", nullable = false)
    private Float floor;

    @Convert(converter = PointListConverter.class)
    @Column(name = "event_area_points", nullable = false)
    private List<Point> points;

    @Column(name = "event_area_description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_area_type", nullable = false)
    private EventAreaType eventAreaType;

    @OneToMany(mappedBy = "eventArea", fetch = FetchType.LAZY)
    private List<EventTrigger> eventTriggers = new ArrayList<>();

    public void updateCustomArea(String areaName, Float floor, List<Point> points, String areaDescription, EventAreaType eventAreaType) {
        if (areaName != null) {
            this.name = areaName;
        }
        if (floor != null) {
            this.floor = floor;
        }
        if (points != null) {
            this.points = normalizePolygon(points);
        }
        if (areaDescription != null) {
            this.description = areaDescription;
        }
        if (eventAreaType != null) {
            this.eventAreaType = eventAreaType;
        }
    }
}
