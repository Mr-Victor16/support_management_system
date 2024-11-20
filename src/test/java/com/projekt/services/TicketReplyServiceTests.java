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

    /**
     * Method: void add(AddTicketReplyRequest request, Principal principal)
     * Description: Checks that a reply is successfully added to an open ticket and saved in the repository.
     * Expected behavior:
     *  - A new reply is added to the ticket.
     *  - Ticket is updated and saved.
     *  - Ticket reply is saved in the TicketReplyRepository.
     */
    @Test
    void addReplyToOpenTicket_ShouldAddReplySuccessfully() {
        long ticketID = 1;
        long userID = 2;
        long statusID = 2;
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
        assertEquals(1, ticket.getReplies().size());
    }

    /**
     * Method: void add(AddTicketReplyRequest request, Principal principal)
     * Description: Checks that adding a reply to a closed ticket is blocked and an exception is thrown.
     * Expected behavior:
     *  - No reply is added to the ticket.
     *  - No changes are saved to the repository.
     *  - UnauthorizedActionException is thrown with the appropriate message.
     */
    @Test
    void addReplyToClosedTicket_ShouldThrowUnauthorizedActionException() {
        long ticketID = 1;
        long userID = 2;
        long statusID = 2;
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
        assertEquals(0, ticket.getReplies().size());
        assertEquals("You do not have permission to add reply to closed ticket", exception.getMessage());
    }
}
