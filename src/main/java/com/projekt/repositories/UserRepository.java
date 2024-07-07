package com.projekt.repositories;

import com.projekt.models.Role;
import com.projekt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByUsernameOrEmail(String username, String email);

    User findByTicketsId(Long id);

    boolean existsByUsernameAndRolesType(String username, Role.Types type);
}
