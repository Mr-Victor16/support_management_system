package com.projekt.services;

import com.projekt.exceptions.NotFoundException;
import com.projekt.models.User;
import com.projekt.repositories.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;

@Service
public class MailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;

    public MailService(JavaMailSender javaMailSender, TemplateEngine templateEngine, UserRepository userRepository) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.userRepository = userRepository;
    }

    public void sendRegisterMessage(Long userID) throws MessagingException {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new NotFoundException("User", userID));

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        helper.setTo(user.getEmail());
        helper.setSubject("Support System - Registration");

        Context context = new Context();
        context.setVariable("username", user.getUsername());
        String html = templateEngine.process("email/register", context);

        helper.setText(html, true);
        javaMailSender.send(mimeMessage);
    }

    public void sendTicketReplyMessage(String userEmail, String ticketName) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        helper.setTo(userEmail);
        helper.setSubject("Support System - New ticket reply");

        Context context = new Context();
        context.setVariable("ticketName", ticketName);
        String html = templateEngine.process("email/ticketReply", context);

        helper.setText(html, true);
        javaMailSender.send(mimeMessage);
    }

    public void sendChangeStatusMessage(Long userID, String ticketTitle, String statusName) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        User user = userRepository.findById(userID)
                .orElseThrow(() -> new NotFoundException("User", userID));

        helper.setTo(user.getEmail());
        helper.setSubject("Support System - Ticket status changed");

        Context context = new Context();
        context.setVariable("ticketName", ticketTitle);
        context.setVariable("status", statusName);
        String html = templateEngine.process("email/ticketStatus", context);

        helper.setText(html, true);
        javaMailSender.send(mimeMessage);
    }
}
