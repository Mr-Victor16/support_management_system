package com.projekt.services;

import com.projekt.models.TicketReply;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

@Service
public interface TicketReplyService {
    void save(TicketReply ticketReply, String name, Integer id) throws MessagingException;

    void deleteById(Integer replyID);

    boolean exists(Integer replyID);
}
