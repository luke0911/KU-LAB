package com.fifthdimension.digital_twin.map.domain;

import com.fifthdimension.digital_twin.global.entity.BaseEntity;
import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.infrastructure.converter.PointListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

import static com.fifthdimension.digital_twin.global.util.PolygonUtils.normalizePolygon;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity(name = "pois")
@SQLRestriction("is_deleted = false")
public class Poi extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poi_id")
    private Long id;

    @Column(name = "poi_name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "map_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map map;

    @Column(name = "poi_floor", nullable = false)
    private Float floor;

    @Convert(converter = PointListConverter.class)
    @Column(name = "poi_points", nullable = false)
    private List<Point> points;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "poi_center_point_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "poi_center_point_y", nullable = false))
    })
    private Point centerPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "poi_category", nullable = false)
    private PoiCategory category;

    @Column(name = "poi_description", columnDefinition = "TEXT")
    private String description;

    public void updatePoiInfo(String poiName, Float poiFloor, List<Point> poiPoints, PoiCategory poiCategory, String poiDescription) {
        if (poiName != null) {
            this.name = poiName;
        }
        if (poiFloor != null) {
            this.floor = poiFloor;
        }
        if (poiPoints != null) {
            this.points = normalizePolygon(poiPoints);
            this.centerPoint = Point.calculateCenterPoint(poiPoints);
        }
        if (poiCategory != null) {
            this.category = poiCategory;
        }
        if (poiDescription != null) {
            this.description = poiDescription;
        }
    }

}
