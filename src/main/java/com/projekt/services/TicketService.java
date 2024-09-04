package com.projekt.services;

import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;

import com.projekt.payload.response.TicketResponse;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public interface TicketService {
    List<TicketResponse> getAll();

    boolean existsById(Long id);

    void delete(Long id, Principal principal);

    TicketResponse getById(Long id, Principal principal);

    void changeStatus(Long ticketID, Long statusID);

    void add(AddTicketRequest request, String username);

    void update(UpdateTicketRequest request, Principal principal);

    boolean isAuthorized(Long ticketID, String username);

    List<TicketResponse> getUserTickets(Principal principal);
}
