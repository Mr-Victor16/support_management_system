package com.projekt.repositories;

import com.projekt.models.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long> {
    boolean existsByNameIgnoreCase(String name);
}
