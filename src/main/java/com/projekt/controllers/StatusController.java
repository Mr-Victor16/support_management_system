package com.projekt.controllers;

import com.projekt.models.Status;
import com.projekt.services.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

@Controller
public class StatusController {
    @Autowired
    private StatusService statusService;

    @GetMapping("/status-list")
    public String showPriorityList(Model model){
        model.addAttribute("status", statusService.loadAll());
        model.addAttribute("use", statusService.statusUse());
        return "status/showList";
    }

    @GetMapping(value = {"/status/edit/{id}","/status/add"})
    public String showFormStatus(@PathVariable(name = "id", required = false) Integer id, Model model){
        model.addAttribute("status", statusService.loadById(id));

        if(id == null || statusService.exists(id) == false){
            return "status/showAddForm";
        }
        return "status/showEditForm";
    }

    @PostMapping(value = {"/status/edit/{id}","/status/add"})
    public String processFormStatus(@Valid @ModelAttribute(name = "status") Status status, BindingResult bindingResult,
                                    @PathVariable(name = "id", required = false) Integer id, Model model){
        if(bindingResult.hasErrors()){
            if(id == null){
                return "status/showAddForm";
            }
            return "status/showEditForm";
        }

        statusService.save(status);

        model.addAttribute("status", statusService.loadAll());
        model.addAttribute("use", statusService.statusUse());
        return "status/showList";
    }

    @GetMapping("/status/delete/{id}")
    public String deleteStatus(@PathVariable(name = "id", required = false) Integer id, Model model){
        statusService.delete(id);

        model.addAttribute("status", statusService.loadAll());
        model.addAttribute("use", statusService.statusUse());
        return "status/showList";
    }
}
