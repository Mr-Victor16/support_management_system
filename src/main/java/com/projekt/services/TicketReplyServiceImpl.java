package com.projekt.services;

import com.projekt.exceptions.*;
import com.projekt.models.Ticket;
import com.projekt.models.TicketReply;
import com.projekt.payload.request.add.AddTicketReplyRequest;
import com.projekt.repositories.TicketReplyRepository;
import com.projekt.repositories.TicketRepository;
import com.projekt.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Objects;

@Service("ticketReplyDetailsService")
public class TicketReplyServiceImpl implements TicketReplyService{
    private final TicketReplyRepository ticketReplyRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final MailService mailService;
    private final UserRepository userRepository;

    public TicketReplyServiceImpl(TicketReplyRepository ticketReplyRepository, TicketRepository ticketRepository, TicketService ticketService, MailService mailService, UserRepository userRepository) {
        this.ticketReplyRepository = ticketReplyRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.mailService = mailService;
        this.userRepository = userRepository;
    }

    @Override
    public void deleteById(Long id) {
        TicketReply ticketReply = ticketReplyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket reply", id));

        ticketReplyRepository.deleteById(ticketReply.getId());
    }

    @Override
    public boolean existsById(Long id) {
        return ticketReplyRepository.existsById(id);
    }

    @Override
    public void add(AddTicketReplyRequest request, Principal principal) {
        Ticket ticket = ticketRepository.findById(request.ticketID())
                .orElseThrow(() -> new NotFoundException("Ticket", request.ticketID()));

        if(!ticketService.isAuthorized(ticket.getId(), principal.getName())){
            throw UnauthorizedActionException.forActionToResource("add reply", "ticket");
        }

        TicketReply ticketReply = new TicketReply();
        ticketReply.setUser(userRepository.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new NotFoundException("User", principal.getName())));
        ticketReplyRepository.save(ticketReply);

        ticket.getReplies().add(ticketReply);

        try {
            if(!Objects.equals(ticket.getUser().getId(), request.userID())){
                mailService.sendTicketReplyMessage(ticket.getUser().getEmail(), ticket.getTitle());
            }
        } catch (MessagingException ex) {
            throw new NotificationFailedException("Error occurred while sending notification", ex);
        }

        ticketRepository.save(ticket);
    }
}
