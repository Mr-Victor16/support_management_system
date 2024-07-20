package com.projekt.repositories;

import com.projekt.models.Role;
import com.projekt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByTicketsId(Long id);

    boolean existsByUsernameAndRolesType(String username, Role.Types type);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findUserByUsername(String username);
}
