package com.projekt.controllers;

import com.projekt.models.*;
import com.projekt.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import java.util.ArrayList;

@Controller
public class SearchController {
    private final SoftwareService softwareService;
    private final StatusService statusService;
    private final PriorityService priorityService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final RoleService roleService;
    private final TicketService ticketService;
    private final SearchService searchService;

    public SearchController(SoftwareService softwareService, StatusService statusService, PriorityService priorityService, CategoryService categoryService, UserService userService, KnowledgeBaseService knowledgeBaseService, RoleService roleService, TicketService ticketService, SearchService searchService) {
        this.softwareService = softwareService;
        this.statusService = statusService;
        this.priorityService = priorityService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.roleService = roleService;
        this.ticketService = ticketService;
        this.searchService = searchService;
    }

    @GetMapping("/knowledge-search")
    public String showKnowledgeSearch(Model model){
        model.addAttribute("knowledgeBase", knowledgeBaseService.loadAll());
        model.addAttribute("search", new Search());
        return "knowledge-base/showList";
    }

    @PostMapping("/knowledge-search")
    public String processSearchKnowledge(@ModelAttribute(name = "search") Search search, Model model){
        model.addAttribute("knowledgeBase",searchService.knowledgeSearch(search));
        return "knowledge-base/showList";
    }

    @GetMapping("/software-search")
    public String showSoftwareSearch(Model model){
        model.addAttribute("software", softwareService.loadAll());
        model.addAttribute("useInTickets", softwareService.softwareUseInTicket());
        model.addAttribute("useInKnowledgeBase", softwareService.softwareUseInKnowledgeBase());
        model.addAttribute("search", new Search());
        return "software/showList";
    }

    @PostMapping("/software-search")
    public String processSearchSoftware(@ModelAttribute(name = "search") Search search, Model model){
        if(search.getPhrase() != ""){
            model.addAttribute("software", softwareService.searchSoftwareByNameDescription(search.getPhrase()));
            model.addAttribute("useInTickets", softwareService.softwareUseInTicket(softwareService.searchSoftwareByNameDescription(search.getPhrase())));
            model.addAttribute("useInKnowledgeBase", softwareService.softwareUseInKnowledgeBase(softwareService.searchSoftwareByNameDescription(search.getPhrase())));
        }else{
            model.addAttribute("software", softwareService.loadAll());
            model.addAttribute("useInTickets", softwareService.softwareUseInTicket());
            model.addAttribute("useInKnowledgeBase", softwareService.softwareUseInKnowledgeBase());
        }

        return "software/showList";
    }

    @GetMapping("/user-search")
    public String showUserSearch(Model model){
        model.addAttribute("user", userService.loadAll());
        model.addAttribute("search", new Search());
        return "user/showList";
    }

    @PostMapping("/user-search")
    public String processUserSearch(@ModelAttribute(name = "search") Search search, Model model){
        if(search.getType() == 1){
            model.addAttribute("user", userService.searchUserByNameSurnameUsername(search.getPhrase()));
        }else if(search.getType() == 2){
            model.addAttribute("user", userService.searchUserByEmail(search.getEmail()));
        }else if(search.getType() == 3){
            if(search.getRole() == null){
                model.addAttribute("user", userService.loadAll());
            }else {
                model.addAttribute("user", userService.searchUserByRole(search.getRole().getId()));
            }
        }

        return "user/showList";
    }

    @GetMapping("/ticket-search")
    public String showTicketSearch(Model model){
        model.addAttribute("ticket", ticketService.loadAll());
        model.addAttribute("search", new Search());
        return "ticket/showList";
    }

    @PostMapping("/ticket-search")
    public String processTicketSearch(@Valid @ModelAttribute(name = "search") Search search, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors() && search.getType() == 4){
            model.addAttribute("ticket", ticketService.loadAll());
            return "ticket/showList";
        }
        model.addAttribute("ticket", searchService.ticketSearch(search));
        return "ticket/showList";
    }

    @ModelAttribute("softwareList")
    public ArrayList<Software> loadSoftware(){
        return softwareService.loadAll();
    }

    @ModelAttribute("statusList")
    public ArrayList<Status> loadStatus(){
        return statusService.loadAll();
    }

    @ModelAttribute("priorityList")
    public ArrayList<Priority> loadPriority(){
        return priorityService.loadAll();
    }

    @ModelAttribute("categoryList")
    public ArrayList<Category> loadCategory(){
        return categoryService.loadAll();
    }

    @ModelAttribute("rolesList")
    public ArrayList<Role> loadUser(){
        return roleService.loadAll();
    }

}
