package com.projekt.services;

import com.projekt.repositories.TicketReplyRepository;
import org.springframework.stereotype.Service;

@Service("ticketReplyDetailsService")
public class TicketReplyServiceImpl implements TicketReplyService{
    private final TicketReplyRepository ticketReplyRepository;

    public TicketReplyServiceImpl(TicketReplyRepository ticketReplyRepository) {
        this.ticketReplyRepository = ticketReplyRepository;
    }

    @Override
    public void deleteById(Long replyID) {
        ticketReplyRepository.deleteById(replyID);
    }

    @Override
    public boolean existsById(Long replyID) {
        return ticketReplyRepository.existsById(replyID);
    }
}
