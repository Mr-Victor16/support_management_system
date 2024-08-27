package com.projekt.services;

public interface TicketReplyService {
    void deleteById(Long replyID);

    boolean existsById(Long replyID);
}
