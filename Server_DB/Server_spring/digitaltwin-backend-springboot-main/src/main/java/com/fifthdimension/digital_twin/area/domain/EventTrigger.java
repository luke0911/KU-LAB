package com.fifthdimension.digital_twin.area.domain;

import com.fifthdimension.digital_twin.global.entity.BaseEntity;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity(name = "event_triggers")
@SQLRestriction("is_deleted = false")
public class EventTrigger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_trigger_id")
    private Long id;

    @Column(name = "event_trigger_name", nullable = false, length = 100)
    private String triggerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_area_id", nullable = false)
    private EventArea eventArea;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "event_trigger_roles", joinColumns = @JoinColumn(name = "event_trigger_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "event_target_role")
    private Set<UserRole> targetUserRoles;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_trigger_type", nullable = false)
    private EventTriggerType triggerType;

    @Column(name = "evnet_message", nullable = false)
    private String eventMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_message_type", nullable = false)
    private EventMessageType eventMessageType;

    @Column(name = "event_delay_ms")
    private Long delay;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public void updateTrigger(String triggerName, Set<UserRole> targetUserRoles, EventTriggerType triggerType, String eventMessage, EventMessageType eventMessageType, Long delay, Boolean isActive) {
        this.triggerName = triggerName;
        this.targetUserRoles = targetUserRoles;
        this.triggerType = triggerType;
        this.eventMessage = eventMessage;
        this.eventMessageType = eventMessageType;
        this.delay = delay;
        this.isActive = isActive;
    }

}
