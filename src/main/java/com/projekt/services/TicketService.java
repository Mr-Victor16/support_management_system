package com.projekt.services;

import com.projekt.models.*;
import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;

import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

public interface TicketService {
    List<Ticket> getAll();

    boolean existsById(Long id);

    void delete(Long id, Principal principal);

    Ticket getById(Long id, Principal principal);

    void changeStatus(Long ticketID, Long statusID);

    void add(AddTicketRequest request, String username);

    void update(UpdateTicketRequest request, Principal principal);

    boolean isAuthorized(Long ticketID, String username);

    List<Ticket> findUserTickets(Principal principal);

    List<Image> processFiles(List<MultipartFile> files);
}
