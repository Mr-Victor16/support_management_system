package com.projekt.repositories;

import com.projekt.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByType(Role.Types type);

    Optional<Role> findRoleByType(Role.Types type);
}
