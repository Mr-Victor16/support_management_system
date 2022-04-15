package com.projekt.services;

import com.projekt.models.Knowledge;
import com.projekt.models.Search;
import com.projekt.models.Ticket;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public interface SearchService {
    ArrayList<Knowledge> knowledgeSearch(Search search);

    ArrayList<Ticket> ticketSearch(Search search);
}
