package com.projekt.repositories;

import com.projekt.models.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketReplyRepository extends JpaRepository<TicketReply, Long> {

}
