package com.projekt.repositories;

import com.projekt.models.Role;
import com.projekt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndRolesType(String username, Role.Types type);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Optional<User> findUserByUsernameIgnoreCase(String username);
}
