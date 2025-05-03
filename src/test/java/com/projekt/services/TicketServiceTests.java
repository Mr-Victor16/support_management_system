package com.projekt.services;

import com.projekt.models.*;
import com.projekt.repositories.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TicketServiceTests {
    private TicketService ticketService;
    private TicketRepository ticketRepository;
    private UserRepository userRepository;
    private StatusRepository statusRepository;
    private JavaMailSender javaMailSender;
    private TemplateEngine templateEngine;
    private MailService mailService;

    @BeforeEach
    public void setUp() {
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        PriorityRepository priorityRepository = mock(PriorityRepository.class);
        SoftwareRepository softwareRepository = mock(SoftwareRepository.class);

        ticketRepository = mock(TicketRepository.class);
        userRepository = mock(UserRepository.class);
        statusRepository = mock(StatusRepository.class);
        javaMailSender = mock(JavaMailSender.class);
        templateEngine = mock(TemplateEngine.class);
        mailService = mock(MailService.class);

        ticketService = new TicketServiceImpl(ticketRepository, mailService, userRepository, statusRepository, categoryRepository, priorityRepository, softwareRepository);
    }

    /**
     * Method: boolean isAuthorized(Long ticketID, String username)
     * Description: Verifies that the method returns 'true' when the user has the role 'Operator'.
     * Expected behavior:
     *  - User is authorized if they have the 'Operator' role.
     */
    @Test
    void isAuthorized_UserIsOperator_ShouldReturnTrue() {
        String username = "nickname";

        when(userRepository.existsByUsernameIgnoreCaseAndRoleType(username, Role.Types.ROLE_OPERATOR)).thenReturn(true);

        assertTrue(ticketService.isAuthorized(1L, username));
    }

    /**
     * Method: boolean isAuthorized(Long ticketID, String username)
     * Description: Verifies that the method returns 'true' when the user owns the ticket.
     * Expected behavior:
     *  - User is authorized if they own the ticket.
     */
    @Test
    void isAuthorized_UserIsTicketOwner_ShouldReturnTrue(){
    String username = "nickname";
        long userID = 1;
        long ticketID = 2;

        User user = new User();
        user.setUsername(username);
        user.setId(userID);

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setId(ticketID);

        when(userRepository.existsByUsernameIgnoreCaseAndRoleType(username, Role.Types.ROLE_OPERATOR)).thenReturn(false);
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(ticketRepository.findById(ticketID)).thenReturn(Optional.of(ticket));

        assertTrue(ticketService.isAuthorized(ticketID, username));
    }

    /**
     * Method: boolean isAuthorized(Long ticketID, String username)
     * Description: Ensures that the method returns 'false' when the user has neither the 'Operator' role nor the ownership of the ticket.
     * Expected behavior:
     *  - User is not authorized if they don't have the 'Operator' role and are not the ticket owner.
     */
    @Test
    void isAuthorized_UserIsNotAuthorized_ShouldReturnFalse() {
        String username = "nickname";
        long userID = 1;
        long ticketID = 2;

        User firstUser = new User();
        firstUser.setUsername(username);
        firstUser.setId(userID);

        User secondUser = new User();
        secondUser.setUsername("name");
        secondUser.setId(2L);

        Ticket ticket = new Ticket();
        ticket.setUser(secondUser);
        ticket.setId(ticketID);

        when(userRepository.existsByUsernameIgnoreCaseAndRoleType(username, Role.Types.ROLE_OPERATOR)).thenReturn(false);
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(firstUser));
        when(ticketRepository.findById(ticketID)).thenReturn(Optional.of(ticket));

        assertFalse(ticketService.isAuthorized(ticketID, username));
    }

    /**
     * Method: void changeStatus(Long ticketID, Long statusID)
     * Description: Verifies the correctness of changing the status of a ticket and sending an email notification.
     * Expected behavior:
     *  - The status of the ticket is updated.
     *  - A notification email is sent to the user after the status change.
     */
    @Test
    void changeStatus_ShouldChangeTicketStatusAndSendEmail() throws MessagingException {
        long ticketID = 1;
        long statusID = 2;
        long newStatusID = 3;
        long userID = 3;

        Status status = new Status();
        status.setId(statusID);
        status.setName("Opened");

        Status newStatus = new Status();
        newStatus.setId(newStatusID);
        newStatus.setName("Closed");

        User user = new User();
        user.setId(userID);
        user.setEmail("email@email.com");

        Ticket ticket = new Ticket();
        ticket.setId(ticketID);
        ticket.setTitle("Title");
        ticket.setUser(user);
        ticket.setStatus(status);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("mocked-html");
        when(statusRepository.findById(newStatusID)).thenReturn(Optional.of(newStatus));
        when(ticketRepository.findById(ticketID)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userID)).thenReturn(Optional.of(user));

        ticketService.changeStatus(ticketID, newStatusID);

        verify(ticketRepository, times(1)).save(ticket);
        verify(mailService, times(1)).sendChangeStatusMessage(userID, ticket.getTitle(), newStatus.getName());
        assertEquals(newStatus.getName(), ticket.getStatus().getName());
    }
}
