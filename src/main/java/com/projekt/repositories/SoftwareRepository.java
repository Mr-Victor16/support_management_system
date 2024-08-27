package com.projekt.repositories;

import com.projekt.models.Software;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoftwareRepository extends JpaRepository<Software, Long> {
    boolean existsByNameIgnoreCase(String name);
}
