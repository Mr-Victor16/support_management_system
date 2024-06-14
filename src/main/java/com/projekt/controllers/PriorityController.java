package com.projekt.controllers;

import com.projekt.models.Priority;
import com.projekt.services.PriorityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

@Controller
public class PriorityController {
    private final PriorityService priorityService;

    public PriorityController(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    @GetMapping("/priority-list")
    public String showPriorityList(Model model){
        model.addAttribute("priority", priorityService.loadAll());
        model.addAttribute("use", priorityService.prioritiesUse());
        return "priority/showList";
    }

    @GetMapping(value = {"/priority/edit/{id}","/priority/add"})
    public String showFormPriority(@PathVariable(name = "id", required = false) Integer id, Model model){
        model.addAttribute("priority", priorityService.loadById(id));

        if(id == null || !priorityService.exists(id)){
            return "priority/showAddForm";
        }
        return "priority/showEditForm";
    }

    @PostMapping(value = {"/priority/edit/{id}","/priority/add"})
    public String processFormPriority(@Valid @ModelAttribute(name = "priority") Priority priority, BindingResult bindingResult,
                                      @PathVariable(name = "id", required = false) Integer id, Model model){
        if(bindingResult.hasErrors()){
            if(id == null){
                return "priority/showAddForm";
            }
            return "priority/showEditForm";
        }

        priorityService.save(priority);

        model.addAttribute("priority", priorityService.loadAll());
        model.addAttribute("use", priorityService.prioritiesUse());
        return "priority/showList";
    }

    @GetMapping("/priority/delete/{id}")
    public String deletePriority(@PathVariable(name = "id", required = false) Integer id, Model model){
        priorityService.delete(id);

        model.addAttribute("priority", priorityService.loadAll());
        model.addAttribute("use", priorityService.prioritiesUse());
        return "priority/showList";
    }
}
