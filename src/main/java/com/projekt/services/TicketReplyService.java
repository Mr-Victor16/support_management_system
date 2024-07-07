package com.projekt.services;

import org.springframework.stereotype.Service;

@Service
public interface TicketReplyService {
    void deleteById(Long replyID);

    boolean existsById(Long replyID);
}
