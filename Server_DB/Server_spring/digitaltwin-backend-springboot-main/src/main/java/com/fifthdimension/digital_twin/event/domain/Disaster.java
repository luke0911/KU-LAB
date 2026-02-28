package com.fifthdimension.digital_twin.event.domain;

import com.fifthdimension.digital_twin.global.entity.BaseEntity;
import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.map.domain.Map;
import com.fifthdimension.digital_twin.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity(name = "disasters")
@SQLRestriction("is_deleted = false")
public class Disaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disaster_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "map_id", nullable = false)
    private Map map;

    @Column(name = "disaster_floor", nullable = false)
    private Float floor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "disaster_point_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "disaster_point_y", nullable = false))
    })
    private Point disasterPoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisasterType disasterType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus eventStatus; // REPORTED, RECEIVED, COMPLETED, CANCELLED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disaster_reporter_id", nullable = false)
    private User reporter;

    @Column(name = "disaster_details", columnDefinition = "TEXT")
    private String details;

    public void receive(){
        this.eventStatus = EventStatus.RECEIVED;
    }

    public void complete(){
        this.eventStatus = EventStatus.COMPLETED;
    }

    public void cancel(){
        this.eventStatus = EventStatus.CANCELLED;
    }

}
