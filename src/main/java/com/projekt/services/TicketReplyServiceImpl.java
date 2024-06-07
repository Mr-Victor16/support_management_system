package com.projekt.services;

import com.projekt.models.TicketReply;
import com.projekt.repositories.TicketReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.time.LocalDate;
import java.util.List;

@Service("ticketReplyDetailsService")
public class TicketReplyServiceImpl implements TicketReplyService{
    @Autowired
    private TicketReplyRepository ticketReplyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

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
