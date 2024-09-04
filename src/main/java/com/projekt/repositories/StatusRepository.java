package com.projekt.repositories;

import com.projekt.models.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    boolean existsByNameIgnoreCase(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Status s SET s.defaultStatus = false WHERE s.defaultStatus = true")
    void clearDefaultStatus();

    Optional<Status> findByDefaultStatusTrue();
}
