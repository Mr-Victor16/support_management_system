package com.projekt.repositories;

import com.projekt.models.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Long> {
    boolean existsByNameIgnoreCase(String name);
}
