package com.projekt.services;

import com.projekt.models.Knowledge;
import com.projekt.models.Search;
import com.projekt.models.Ticket;
import com.projekt.repositories.KnowledgeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

@Service("searchServiceImpl")
public class SearchServiceImpl implements SearchService{
    private final KnowledgeBaseService knowledgeBaseService;
    private final TicketService ticketService;
    private final KnowledgeRepository knowledgeRepository;

    public SearchServiceImpl(KnowledgeBaseService knowledgeBaseService, TicketService ticketService, KnowledgeRepository knowledgeRepository) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.ticketService = ticketService;
        this.knowledgeRepository = knowledgeRepository;
    }

    @Override
    public ArrayList<Knowledge> knowledgeSearch(Search search) {
        if(search.getType() == 1){
            return knowledgeBaseService.searchKnowledgeByTitleContent(search.getPhrase());
        }
        else if(search.getType() == 2){
            if(search.getDate1() == null && search.getDate2() == null){
                return (ArrayList<Knowledge>) knowledgeRepository.findAll();
            } else if(search.getDate2() == null){
                return knowledgeBaseService.searchKnowledgeByDate(search.getDate1(), LocalDate.now());
            } else if(search.getDate1() == null){
                return knowledgeBaseService.searchKnowledgeByDate(LocalDate.of(1970,1,1), search.getDate2());
            } else {
                return knowledgeBaseService.searchKnowledgeByDate(search.getDate1(), search.getDate2());
            }
        }
        else if(search.getType() == 3){
            return knowledgeBaseService.searchKnowledgeBySoftware(search.getSoftware());
        }

        return knowledgeBaseService.loadAll();
    }

    @Override
    public ArrayList<Ticket> ticketSearch(Search search) {
        if(search.getType() == 1){
            return ticketService.searchByPhrase(search.getPhrase());
        }else if(search.getType() == 2){
            return ticketService.searchByDate(search.getDate1(), search.getDate2());
        }else if(search.getType() == 3){
            return ticketService.searchBySoftware(search.getSoftware());
        }else if(search.getType() == 4){
            if(search.getVersion() == null){
                return ticketService.loadAll();
            }else{
                return ticketService.searchByVersion(search.getVersion());
            }
        }else if(search.getType() == 5){
            return ticketService.searchByStatus(search.getStatus());
        }else if(search.getType() == 6){
            return ticketService.searchByPriority(search.getPriority());
        }else if(search.getType() == 7){
            return ticketService.searchByCategory(search.getCategories());
        }else if(search.getType() == 8){
            return ticketService.searchByReplyNumber(search.getNumber1(), search.getNumber2());
        }
        return ticketService.loadAll();
    }
}
