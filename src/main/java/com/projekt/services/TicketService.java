package com.projekt.services;

import com.projekt.models.*;
import com.projekt.payload.request.add.AddTicketReplyRequest;
import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.edit.EditTicketRequest;
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

    void addReply(AddTicketReplyRequest ticketReply) throws MessagingException;

    void changeStatus(Long ticketID, Long statusID) throws MessagingException;

    Ticket findByImageId(Long imageId);

    void add(AddTicketRequest request, String username);

    void update(EditTicketRequest request);

    void addImage(Long ticketID, MultipartFile file) throws IOException;

    boolean existsByCategoryId(Long categoryID);

    boolean entitiesExist(Long categoryID, Long statusID, Long priorityID, Long softwareID);
}
