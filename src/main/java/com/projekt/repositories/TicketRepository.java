package com.projekt.repositories;

import com.projekt.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Long countByCategoryId(Long categoryID);

    Long countByPriorityId(Long priorityID);

    Long countBySoftwareId(Long id);

    Long countByStatusId(Long statusID);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByPriorityId(Long priorityId);

    boolean existsByStatusId(Long statusID);

    boolean existsBySoftwareId(Long softwareID);

    Ticket findByImagesId(Long imageID);
}
