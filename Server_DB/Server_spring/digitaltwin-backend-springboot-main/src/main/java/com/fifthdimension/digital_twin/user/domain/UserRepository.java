package com.fifthdimension.digital_twin.user.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByAccountId(String accountId);
    Optional<User> findByAccountIdAndIsDeletedIsFalse(String accountId);
    Optional<User> findByIdAndIsDeletedIsFalse(UUID userId);

    boolean existsByAccountId(String accountId);

    Page<User> findAllByRoleNotAndIsDeletedIsFalse(UserRole excludedRole, Pageable pageable);
    Page<User> findAllByAccountIdContainingAndRoleNotAndIsDeletedIsFalse(String accountId, UserRole excludedRole, Pageable pageable);
    Page<User> findAllByNameContainingAndRoleNotAndIsDeletedIsFalse(String name, UserRole excludedRole, Pageable pageable);

    Page<User> findAllByAccountIdContaining(String accountId, Pageable pageable);
    Page<User> findAllByNameContaining(String name, Pageable pageable);
}
