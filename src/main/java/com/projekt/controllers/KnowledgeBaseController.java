package com.projekt.controllers;

import com.projekt.models.Knowledge;
import com.projekt.models.Search;
import com.projekt.models.Software;
import com.projekt.services.KnowledgeBaseService;
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
import java.util.ArrayList;

@Controller
public class KnowledgeBaseController {
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private SoftwareService softwareService;

    @GetMapping("/knowledge-base")
    public String showKnowledgeBaseList(Model model){
        model.addAttribute("knowledgeBase", knowledgeBaseService.loadAll());
        model.addAttribute("search", new Search());
        return "knowledge-base/showList";
    }

    @GetMapping("/knowledge-base/{id}")
    public String showKnowledgeBase(@PathVariable(name = "id", required = false) Integer id, Model model){
        if(knowledgeBaseService.exists(id)) {
            model.addAttribute("knowledgeBaseItem", knowledgeBaseService.loadById(id));
            return "knowledge-base/showItem";
        }
        model.addAttribute("knowledgeBase", knowledgeBaseService.loadAll());
        model.addAttribute("search", new Search());
        return "knowledge-base/showList";
    }

    @GetMapping(value = {"/knowledge/edit/{id}","/knowledge/add"})
    public String showFormKnowledgeBase(@PathVariable(name = "id", required = false) Integer id, Model model){
        model.addAttribute("knowledge", knowledgeBaseService.loadById(id));

        if(id == null || knowledgeBaseService.exists(id) == false){
            return "knowledge-base/showAddForm";
        }
        return "knowledge-base/showEditForm";
    }

    @PostMapping(value = {"/knowledge/edit/{id}","/knowledge/add"})
    public String processFormKnowledgeBase(@Valid @ModelAttribute(name = "knowledge") Knowledge knowledge, BindingResult bindingResult,
                                           @PathVariable(name = "id", required = false) Integer id, Model model){
        if(bindingResult.hasErrors()){
            if(id == null){
                return "knowledge-base/showAddForm";
            }
            return "knowledge-base/showEditForm";
        }

        knowledgeBaseService.save(knowledge);

        model.addAttribute("knowledgeBase", knowledgeBaseService.loadAll());
        model.addAttribute("search", new Search());
        return "knowledge-base/showList";
    }

    @GetMapping("/knowledge/delete/{id}")
    public String deleteKnowledgeBase(@PathVariable(name = "id", required = false) Integer id, Model model){
        knowledgeBaseService.delete(id);

        model.addAttribute("knowledgeBase", knowledgeBaseService.loadAll());
        model.addAttribute("search", new Search());
        return "knowledge-base/showList";
    }

    @ModelAttribute("softwareList")
    public ArrayList<Software> loadSoftware(){
        return softwareService.loadAll();
    }

}
