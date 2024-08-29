package com.projekt.services;

import com.projekt.payload.request.add.AddTicketReplyRequest;

import java.security.Principal;

public interface TicketReplyService {
    void deleteById(Long id);

    boolean existsById(Long id);

    void add(AddTicketReplyRequest request, Principal principal);
}
