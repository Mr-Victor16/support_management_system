package com.projekt.services;

import com.projekt.models.TicketReply;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.util.List;

@Service
public interface TicketReplyService {
    void save(TicketReply ticketReply, String name, Integer id) throws MessagingException;

    void deleteById(Integer replyID);

    void deleteAll(List<TicketReply> ticketReplies);

    boolean exists(Integer replyID);
}
