package com.projekt.services;

import com.projekt.models.*;
import com.projekt.payload.request.add.AddTicketReply;
import com.projekt.repositories.*;
import com.projekt.security.jwt.JWTUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TicketServiceTests {
    private TicketService ticketService;
    private TicketRepository ticketRepository;
    private TicketReplyRepository ticketReplyRepository;
    private UserRepository userRepository;
    private StatusRepository statusRepository;
    private JavaMailSender javaMailSender;
    private TemplateEngine templateEngine;

    @BeforeEach
    public void setUp() {
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        PriorityRepository priorityRepository = mock(PriorityRepository.class);
        SoftwareRepository softwareRepository = mock(SoftwareRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JWTUtils jwtUtils = mock(JWTUtils.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        ticketRepository = mock(TicketRepository.class);
        ticketReplyRepository = mock(TicketReplyRepository.class);
        userRepository = mock(UserRepository.class);
        statusRepository = mock(StatusRepository.class);
        javaMailSender = mock(JavaMailSender.class);
        templateEngine = mock(TemplateEngine.class);

        MailService mailService = new MailService(javaMailSender, templateEngine, userRepository);
        UserService userService = new UserServiceImpl(userRepository, mailService, roleRepository, passwordEncoder, jwtUtils, authenticationManager);

        ticketService = new TicketServiceImpl(ticketRepository, userService, mailService, ticketReplyRepository, userRepository, statusRepository, categoryRepository, priorityRepository, softwareRepository);
    }

    //boolean isAuthorized(Long ticketID, String username);
    //Verifies that the method returns ‘true’ when the user has the role 'Operator'.
    @Test
    void testIsAuthorized_shouldReturnTrue_userIsOperator() {
        String username = "nickname";

        when(userRepository.existsByUsernameIgnoreCaseAndRolesType(username, Role.Types.ROLE_OPERATOR)).thenReturn(true);

        assertTrue(ticketService.isAuthorized(1L, username));
    }

    //boolean isAuthorized(Long ticketID, String username);
    //Verifies that the method returns ‘true’ when the user owns the ticket.
    @Test
    void testIsAuthorized_shouldReturnTrue_userIsTicketOwner() {
        String username = "nickname";
        Long userID = 1L;
        Long ticketID = 2L;

        User user = new User();
        user.setUsername(username);
        user.setId(userID);

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setId(ticketID);

        when(userRepository.existsByUsernameIgnoreCaseAndRolesType(username, Role.Types.ROLE_OPERATOR)).thenReturn(false);
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(user);
        when(ticketRepository.getReferenceById(ticketID)).thenReturn(ticket);

        assertTrue(ticketService.isAuthorized(ticketID, username));
    }

    //boolean isAuthorized(Long ticketID, String username);
    //Ensures that the method returns ‘false’ when the user has neither the ‘Operator’ role nor the owner of the request.
    @Test
    void testIsAuthorized_shouldReturnFalse_userIsNotAuthorized() {
        String username = "nickname";
        Long userID = 1L;
        Long ticketID = 2L;

        User user = new User();
        user.setUsername(username);
        user.setId(userID);

        User user2 = new User();
        user2.setUsername("name");
        user2.setId(2L);

        Ticket ticket = new Ticket();
        ticket.setUser(user2);
        ticket.setId(ticketID);

        when(userRepository.existsByUsernameIgnoreCaseAndRolesType(username, Role.Types.ROLE_OPERATOR)).thenReturn(false);
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(user);
        when(ticketRepository.getReferenceById(ticketID)).thenReturn(ticket);

        assertFalse(ticketService.isAuthorized(ticketID, username));
    }

    //void addReply(AddTicketReplyRequest ticketReply) throws MessagingException;
    //Checks that the response to the ticket is added correctly.
    @Test
    void testAddReply() throws MessagingException {
        Long ticketID = 1L;
        Long userID = 2L;

        AddTicketReply request = new AddTicketReply(ticketID, userID, "content", LocalDate.now());

        User user = new User();
        user.setId(userID);

        Ticket ticket = new Ticket();
        ticket.setId(ticketID);
        ticket.setUser(user);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("mocked-html");
        when(userRepository.getReferenceById(userID)).thenReturn(user);
        when(ticketRepository.getReferenceById(ticketID)).thenReturn(ticket);

        ticketService.addReply(request);

        verify(ticketReplyRepository, times(1)).save(any(TicketReply.class));
        verify(ticketRepository, times(1)).save(ticket);
        assertEquals(ticket.getReplies().size(), 1);
    }

    //void changeStatus(Long ticketID, Long statusID) throws MessagingException;
    //Verifies the correctness of the change in the status of the ticket.
    @Test
    void testChangeStatus() throws MessagingException {
        Long ticketID = 1L;
        Long statusID = 2L;
        Long userID = 3L;

        Status status = new Status();
        status.setId(statusID);
        status.setName("Closed");

        User user = new User();
        user.setId(userID);
        user.setEmail("email@email.com");

        Ticket ticket = new Ticket();
        ticket.setId(ticketID);
        ticket.setUser(user);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("mocked-html");
        when(statusRepository.getReferenceById(statusID)).thenReturn(status);
        when(ticketRepository.getReferenceById(ticketID)).thenReturn(ticket);
        when(userRepository.getReferenceById(userID)).thenReturn(user);

        ticketService.changeStatus(ticketID, statusID);

        verify(userRepository, times(1)).getReferenceById(userID);
        verify(ticketRepository, times(1)).save(ticket);
        assertEquals(status.getName(), ticket.getStatus().getName());
    }
}
