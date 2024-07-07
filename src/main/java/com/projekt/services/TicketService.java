package com.projekt.services;

import com.projekt.models.*;
import com.projekt.payload.request.AddTicketReply;
import com.projekt.payload.request.AddTicketRequest;
import com.projekt.payload.request.EditTicketRequest;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface TicketService {
    List<Ticket> getAll();

    boolean existsById(Long id);

    void delete(Long id);

    boolean isAuthorized(Long ticketID, String username);

    Ticket getById(Long id);

    List<Ticket> getTicketsByUserId(Long id);

    void addReply(AddTicketReply ticketReply) throws MessagingException;

    void changeStatus(Long ticketID, Long statusID) throws MessagingException;

    Ticket findByImageId(Long imageId);

    void add(AddTicketRequest request);

    void update(EditTicketRequest request);

    void addImage(Long ticketID, MultipartFile file) throws IOException;

    boolean entitiesExist(Long categoryID, Long statusID, Long priorityID, Long softwareID);
}
