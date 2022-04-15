package com.projekt.controllers;

import com.projekt.models.Search;
import com.projekt.models.Software;
import com.projekt.services.SoftwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class SoftwareController {
    @Autowired
    private SoftwareService softwareService;

    @GetMapping("/software-list")
    public String showSoftwareList(Model model){
        model.addAttribute("software", softwareService.loadAll());
        model.addAttribute("useInTickets", softwareService.softwareUseInTicket());
        model.addAttribute("useInKnowledgeBase", softwareService.softwareUseInKnowledgeBase());
        model.addAttribute("search", new Search());
        return "software/showList";
    }

    @GetMapping("/software-list/{id}")
    public String showSoftware(@PathVariable(name = "id", required = false) Integer id, Model model){
        if(softwareService.exists(id)){
            model.addAttribute("softwareItem", softwareService.loadById(id));
            return "software/showItem";
        }

        model.addAttribute("software", softwareService.loadAll());
        model.addAttribute("useInTickets", softwareService.softwareUseInTicket());
        model.addAttribute("useInKnowledgeBase", softwareService.softwareUseInKnowledgeBase());
        model.addAttribute("search", new Search());
        return "software/showList";
    }

    @GetMapping(value = {"/software/edit/{id}","/software/add"})
    public String showFormSoftware(@PathVariable(name = "id", required = false) Integer id, Model model){
        model.addAttribute("software", softwareService.loadById(id));

        if(id == null || softwareService.exists(id) == false){
            return "software/showAddForm";
        }
        return "software/showEditForm";
    }

    @PostMapping(value = {"/software/edit/{id}","/software/add"})
    public String processFormSoftware(@Valid @ModelAttribute(name = "software") Software software, BindingResult bindingResult,
                                           @PathVariable(name = "id", required = false) Integer id, Model model){
        if(bindingResult.hasErrors()){
            if(id == null){
                return "software/showAddForm";
            }
            return "software/showEditForm";
        }

        softwareService.save(software);

        model.addAttribute("software", softwareService.loadAll());
        model.addAttribute("useInTickets", softwareService.softwareUseInTicket());
        model.addAttribute("useInKnowledgeBase", softwareService.softwareUseInKnowledgeBase());
        model.addAttribute("search", new Search());
        return "software/showList";
    }

    @GetMapping("/software/delete/{id}")
    public String deleteSoftware(@PathVariable(name = "id", required = false) Integer id, Model model){
        softwareService.delete(id);

        model.addAttribute("software", softwareService.loadAll());
        model.addAttribute("useInTickets", softwareService.softwareUseInTicket());
        model.addAttribute("useInKnowledgeBase", softwareService.softwareUseInKnowledgeBase());
        model.addAttribute("search", new Search());
        return "software/showList";
    }
}
