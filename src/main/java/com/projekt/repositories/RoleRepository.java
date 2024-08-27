package com.projekt.repositories;

import com.projekt.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByType(Role.Types type);

    Optional<Role> findRoleByType(Role.Types type);
}
