package com.projekt.services;

import com.projekt.exceptions.UnauthorizedActionException;
import com.projekt.models.Status;
import com.projekt.models.Ticket;
import com.projekt.models.TicketReply;
import com.projekt.models.User;
import com.projekt.payload.request.add.AddTicketReplyRequest;
import com.projekt.repositories.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.Principal;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class TicketReplyServiceTests {
    private TicketRepository ticketRepository;
    private TicketReplyRepository ticketReplyRepository;
    private UserRepository userRepository;
    private JavaMailSender javaMailSender;
    private TemplateEngine templateEngine;
    private TicketReplyService ticketReplyService;

    @BeforeEach
    public void setUp() {
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        PriorityRepository priorityRepository = mock(PriorityRepository.class);
        SoftwareRepository softwareRepository = mock(SoftwareRepository.class);

        ticketRepository = mock(TicketRepository.class);
        ticketReplyRepository = mock(TicketReplyRepository.class);
        userRepository = mock(UserRepository.class);
        StatusRepository statusRepository = mock(StatusRepository.class);
        javaMailSender = mock(JavaMailSender.class);
        templateEngine = mock(TemplateEngine.class);

        MailService mailService = new MailService(javaMailSender, templateEngine, userRepository);

        TicketService ticketService = new TicketServiceImpl(ticketRepository, mailService, userRepository, statusRepository, categoryRepository, priorityRepository, softwareRepository);

        ticketReplyService = new TicketReplyServiceImpl(ticketReplyRepository, ticketRepository, ticketService, mailService, userRepository);
    }

    //public void add(AddTicketReplyRequest request, Principal principal);
    //Checks that the response to the ticket is added correctly.
    @Test
    void testAddReply() {
        Long ticketID = 1L;
        Long userID = 2L;
        Long statusID = 2L;
        String username = "nickname";

        AddTicketReplyRequest request = new AddTicketReplyRequest(ticketID, "content");

        User user = new User();
        user.setId(userID);
        user.setUsername(username);

        Status status = new Status(statusID, "Opened", false);

        Ticket ticket = new Ticket();
        ticket.setId(ticketID);
        ticket.setUser(user);
        ticket.setStatus(status);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("mocked-html");
        when(userRepository.findById(userID)).thenReturn(Optional.of(user));
        when(ticketRepository.findById(ticketID)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(username);

        ticketReplyService.add(request, principal);

        verify(ticketReplyRepository, times(1)).save(any(TicketReply.class));
        verify(ticketRepository, times(1)).save(ticket);
        assertEquals(ticket.getReplies().size(), 1);
    }

    //public void add(AddTicketReplyRequest request, Principal principal);
    //Check if a response to the ticket is blocked when it is closed.
    @Test
    void testAddReplyWhenTicketIsClosed() {
        Long ticketID = 1L;
        Long userID = 2L;
        Long statusID = 2L;
        String username = "nickname";

        AddTicketReplyRequest request = new AddTicketReplyRequest(ticketID, "content");

        User user = new User();
        user.setId(userID);
        user.setUsername(username);

        Status status = new Status(statusID, "Opened", true);

        Ticket ticket = new Ticket();
        ticket.setId(ticketID);
        ticket.setUser(user);
        ticket.setStatus(status);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("mocked-html");
        when(userRepository.findById(userID)).thenReturn(Optional.of(user));
        when(ticketRepository.findById(ticketID)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(username);

        Exception exception = assertThrows(UnauthorizedActionException.class, () -> {
            ticketReplyService.add(request, principal);
        });

        verify(ticketReplyRepository, times(0)).save(any(TicketReply.class));
        verify(ticketRepository, times(0)).save(ticket);
        assertEquals(ticket.getReplies().size(), 0);
        assertEquals("You do not have permission to add reply to closed ticket", exception.getMessage());
    }
}
