package com.projekt.repositories;

import com.projekt.models.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
    boolean existsByNameIgnoreCase(String name);
}
