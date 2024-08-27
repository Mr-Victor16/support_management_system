package com.projekt.repositories;

import com.projekt.models.Role;
import com.projekt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndRolesType(String username, Role.Types type);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Optional<User> findUserByUsernameIgnoreCase(String username);
}
