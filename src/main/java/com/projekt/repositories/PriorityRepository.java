package com.projekt.repositories;

import com.projekt.models.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriorityRepository extends JpaRepository<Priority, Long> {
    boolean existsByNameIgnoreCase(String name);
}
