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
@Entity(name = "accidents")
@SQLRestriction("is_deleted = false")
public class Accident extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accident_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "map_id", nullable = false)
    private Map map;

    @Column(name = "accident_floor", nullable = false)
    private Float floor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "accident_point_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "accident_point_y", nullable = false))
    })
    private Point accidentPoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccidentType accidentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus eventStatus; // REPORTED, RECEIVED, COMPLETED, CANCELLED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accident_reporter_id", nullable = false)
    private User reporter;

    @Column(name = "accident_details", columnDefinition = "TEXT")
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
