package com.fifthdimension.digital_twin.pushmsg.domain;

import com.fifthdimension.digital_twin.event.domain.DevicePlatform;
import com.fifthdimension.digital_twin.global.entity.BaseEntity;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity(name = "fcm_tokens")
public class FCMToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "fcm_token", nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_platform", nullable = false)
    private DevicePlatform platform;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    @Setter
    private Boolean isActive;

    public void updateRole(UserRole role) {
        this.role = role;
    }

    public void updateOwner(UUID userId, UserRole role) {
        this.userId = userId;
        this.role = role;
        this.isActive = true;
    }

}
