package com.projekt.services;

import com.projekt.payload.request.add.AddTicketReplyRequest;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public interface TicketReplyService {
    void deleteById(Long id);

    boolean existsById(Long id);

    void add(AddTicketReplyRequest request, Principal principal);
}
