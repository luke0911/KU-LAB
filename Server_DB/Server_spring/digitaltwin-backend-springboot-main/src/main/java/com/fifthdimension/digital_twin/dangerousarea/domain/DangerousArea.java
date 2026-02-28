package com.fifthdimension.digital_twin.dangerousarea.domain;

import com.fifthdimension.digital_twin.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "dangerous_areas")
@SQLRestriction("is_deleted = 0")
public class DangerousArea extends BaseEntity {

    /** PK: area_id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Long areaId;

    /** 맵 ID (FK) */
    @Column(name = "map_id", nullable = false)
    private Integer mapId;

    /** 위험구역 이름 */
    @Column(name = "dangerous_area_name", nullable = false, length = 100)
    private String areaName;

    /** 층 정보 */
    @Column(name = "dangerous_area_floor", nullable = false)
    private Integer areaFloor;

    /** 구역 타입 */
    @Column(name = "dangerous_area_type", nullable = false)
    private String areaType;

    /** 폴리곤 좌표 (x,y,z) */
    @ElementCollection
    @CollectionTable(name = "dangerous_area_points", joinColumns = @JoinColumn(name = "area_id"))
    private List<Point3D> areaPoints;

    /** 설명 */
    @Column(name = "dangerous_area_descripting", columnDefinition = "TEXT")
    private String areaDescripting;

    /** 입장 시간 */
    @Column(name = "entry_time")
    private String entryTime;

    /** 퇴장 시간 */
    @Column(name = "exit_time")
    private String exitTime;

    public void updateDangerousArea(
            String areaName,
            Integer areaFloor,
            String areaType,
            List<Point3D> areaPoints,
            String areaDescripting,
            String entryTime,
            String exitTime
    ) {
        if (areaName != null) this.areaName = areaName;
        if (areaFloor != null) this.areaFloor = areaFloor;
        if (areaType != null) this.areaType = areaType;
        if (areaPoints != null) this.areaPoints = areaPoints;
        if (areaDescripting != null) this.areaDescripting = areaDescripting;
        if (entryTime != null) this.entryTime = entryTime;
        if (exitTime != null) this.exitTime = exitTime;
    }
}
