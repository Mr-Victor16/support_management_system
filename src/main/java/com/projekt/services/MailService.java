package com.projekt.services;

import com.projekt.models.User;
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
    private final UserService userService;

    public MailService(JavaMailSender javaMailSender, TemplateEngine templateEngine, UserService userService) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.userService = userService;
    }

    public void sendRegisterMessage(String to, String username, boolean enabled) throws MessagingException {
        var mimeMessage = javaMailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mimeMessage, "utf-8");
        //helper.setFrom("noreply@uph.edu.pl");
        helper.setTo(to);
        helper.setSubject("Wsparcie techniczne - Rejestracja");

        var context = new Context();
        context.setVariable("username", username);
        User user = userService.findUserByUsername(username);
        String link = "http://localhost:8080/activate/"+user.getId();
        context.setVariable("link", link);
        context.setVariable("enabled", enabled);
        String html = templateEngine.process("email/register", context);

        helper.setText(html, true);
        javaMailSender.send(mimeMessage);
    }

    public void sendTicketReplyMessage(String to, String ticketName) throws MessagingException {
        var mimeMessage = javaMailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mimeMessage, "utf-8");
        //helper.setFrom("noreply@uph.edu.pl");
        helper.setTo(to);
        helper.setSubject("Wsparcie techniczne - Odpowiedź na zgłoszenie");

        var context = new Context();
        context.setVariable("ticketName", ticketName);
        String html = templateEngine.process("email/ticketReply", context);

        helper.setText(html, true);
        javaMailSender.send(mimeMessage);
    }


    public void sendChangeStatusMessage(Integer id, String ticketTitle, String statusName) throws MessagingException {
        var mimeMessage = javaMailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mimeMessage, "utf-8");
        //helper.setFrom("noreply@uph.edu.pl");
        helper.setTo(userService.loadById(id).getEmail());
        helper.setSubject("Wsparcie techniczne - Zmiana statusu zgłoszenia");

        var context = new Context();
        context.setVariable("ticketName", ticketTitle);
        context.setVariable("status", statusName);
        String html = templateEngine.process("email/ticketStatus", context);

        helper.setText(html, true);
        javaMailSender.send(mimeMessage);
    }
}
