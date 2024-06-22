package com.projekt.services;

import com.projekt.models.TicketReply;
import com.projekt.repositories.TicketReplyRepository;
import com.projekt.repositories.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.time.LocalDate;

@Service("ticketReplyDetailsService")
public class TicketReplyServiceImpl implements TicketReplyService{
    private final TicketReplyRepository ticketReplyRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;

    public TicketReplyServiceImpl(TicketReplyRepository ticketReplyRepository, UserRepository userRepository, TicketService ticketService) {
        this.ticketReplyRepository = ticketReplyRepository;
        this.userRepository = userRepository;
        this.ticketService = ticketService;
    }

    @Override
    public void save(TicketReply ticketReply, String name, Integer id) throws MessagingException {
        ticketReply.setDate(LocalDate.now());
        ticketReply.setUser(userRepository.findByUsername(name));
        ticketReplyRepository.save(ticketReply);

        ticketService.addReply(ticketReply, id);
    }

    @Override
    public void deleteById(Integer replyID) {
        if(ticketReplyRepository.existsById(replyID)){
            ticketReplyRepository.deleteById(replyID);
        }
    }

    @Override
    public boolean exists(Integer replyID) {
        return ticketReplyRepository.existsById(replyID);
    }

}
