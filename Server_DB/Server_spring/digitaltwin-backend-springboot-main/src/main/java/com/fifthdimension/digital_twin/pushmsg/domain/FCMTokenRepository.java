package com.fifthdimension.digital_twin.pushmsg.domain;

import com.fifthdimension.digital_twin.event.domain.DevicePlatform;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
    List<FCMToken> findAllByRoleAndPlatformAndIsActiveTrue(UserRole role, DevicePlatform platform);

    Optional<FCMToken> findByToken(String token);
    Optional<FCMToken> findByTokenAndIsActiveTrue(String token);
    Optional<FCMToken> findByUserIdAndTokenAndPlatformAndIsActiveTrue(UUID userId, String token, DevicePlatform platform);

    List<FCMToken> findAllByUserIdAndPlatformAndIsActiveTrue(UUID userId, DevicePlatform platform);
    List<FCMToken> findAllByUserIdAndIsActiveTrue(UUID userId);
    List<FCMToken> findAllByRoleAndIsActiveTrue(UserRole role);
}
