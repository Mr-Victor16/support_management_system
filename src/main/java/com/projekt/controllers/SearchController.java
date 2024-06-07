package com.projekt.controllers;

import com.projekt.formatters.VersionFormatter;
import com.projekt.models.*;
import com.projekt.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import java.util.ArrayList;

@Controller
public class SearchController {
    @Autowired
    private SoftwareService softwareService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private PriorityService priorityService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SearchService searchService;

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

    @InitBinder
    public void initFormatters(WebDataBinder binder){
        binder.addCustomFormatter(new VersionFormatter());
    }
}
