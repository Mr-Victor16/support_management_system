package com.projekt.converter;

import com.projekt.models.Ticket;
import com.projekt.models.TicketReply;
import com.projekt.payload.response.TicketReplyResponse;
import com.projekt.payload.response.TicketResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TicketConverter {
    private static TicketReplyResponse toTicketReplyResponse(TicketReply reply) {
        return new TicketReplyResponse(
                reply.getId(),
                UserConverter.toUserDetailsResponse(reply.getUser()),
                reply.getContent(),
                reply.getCreatedDate()
        );
    }

    public static TicketResponse toTicketResponse(Ticket ticket){
        List<TicketReplyResponse> replies = ticket.getReplies().stream()
                .map(reply -> toTicketReplyResponse(reply))
                .toList();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getImages(),
                ticket.getCreatedDate(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getVersion(),
                ticket.getSoftware(),
                replies,
                UserConverter.toUserDetailsResponse(ticket.getUser())
        );
    }
}
