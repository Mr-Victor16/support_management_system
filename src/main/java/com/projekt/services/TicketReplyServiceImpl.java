package com.projekt.services;

import com.projekt.models.TicketReply;
import com.projekt.repositories.TicketReplyRepository;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.time.LocalDate;
import java.util.List;

@Service("ticketReplyDetailsService")
public class TicketReplyServiceImpl implements TicketReplyService{
    private final TicketReplyRepository ticketReplyRepository;
    private final UserService userService;
    private final TicketService ticketService;

    public TicketReplyServiceImpl(TicketReplyRepository ticketReplyRepository, UserService userService, TicketService ticketService) {
        this.ticketReplyRepository = ticketReplyRepository;
        this.userService = userService;
        this.ticketService = ticketService;
    }

    @Override
    public void save(TicketReply ticketReply, String name, Integer id) throws MessagingException {
        ticketReply.setReplyDate(LocalDate.now());
        ticketReply.setUser(userService.findUserByUsername(name));
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
    public void deleteAll(List<TicketReply> ticketReplies) {
        for(int i=0; i<ticketReplies.size(); i++) {
            ticketReplyRepository.deleteById(ticketReplies.get(i).getReplyID());
        }
    }

    @Override
    public boolean exists(Integer replyID) {
        return ticketReplyRepository.existsById(replyID);
    }

}
