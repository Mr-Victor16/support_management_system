package com.projekt.repositories;

import com.projekt.models.Knowledge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {
    Long countBySoftwareId(Long id);

    Knowledge findByTitleIgnoreCase(String title);

    boolean existsBySoftwareId(Long softwareID);
}
