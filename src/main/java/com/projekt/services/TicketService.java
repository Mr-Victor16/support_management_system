package com.projekt.services;

import com.projekt.models.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public interface TicketService {
    ArrayList<Ticket> loadAll();

    Ticket loadById(Integer id);

    boolean exists(Integer id);

    Integer save(Ticket ticket, List<MultipartFile> multipartFile, String name) throws IOException;

    void delete(Integer id);

    boolean isAuthorized(Integer id, String name);

    Ticket loadTicketById(Integer id);

    ArrayList<Ticket> loadTicketsByUser(String name);

    void addReply(TicketReply ticketReply, Integer id) throws MessagingException;

    void changeStatus(Integer id, Status status) throws MessagingException;

    ArrayList<Ticket> searchByPhrase(String phrase);

    ArrayList<Ticket> searchByDate(LocalDate date1, LocalDate date2);

    ArrayList<Ticket> searchBySoftware(Software software);

    ArrayList<Ticket> searchByStatus(Status status);

    ArrayList<Ticket> searchByPriority(Priority priority);

    ArrayList<Ticket> searchByVersion(String version);

    ArrayList<Ticket> searchByCategory(Set<Category> categories);

    ArrayList<Ticket> searchByReplyNumber(int number1, int number2);
}
