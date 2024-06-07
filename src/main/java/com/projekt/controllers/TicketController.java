package com.projekt.controllers;

import com.itextpdf.text.DocumentException;
import com.projekt.exceptions.TicketNotFoundException;
import com.projekt.formatters.VersionFormatter;
import com.projekt.models.*;
import com.projekt.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TicketController {
    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketReplyService ticketReplyService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private PriorityService priorityService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SoftwareService softwareService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @Autowired
    private PDFService pdfService;

    @GetMapping("/tickets")
    public String showTicketList(Model model){
        model.addAttribute("ticket", ticketService.loadAll());
        model.addAttribute("search", new Search());
        return "ticket/showList";
    }

    @GetMapping("/my-tickets")
    public String showMyTicketsList(Model model, Principal principal){
        model.addAttribute("ticket", ticketService.loadTicketsByUser(principal.getName()));
        model.addAttribute("search", new Search());
        return "ticket/showList";
    }

    @GetMapping("/ticket/{id}")
    public String showTicket(@PathVariable(name = "id", required = false) Integer id, Model model, Principal principal){
        if(ticketService.isAuthorized(id, principal.getName())){
            model.addAttribute("ticket", ticketService.loadTicketById(id));
            model.addAttribute("ticketReply", new TicketReply());
            model.addAttribute("status", new Status());

            return "ticket/showItem";
        }else{
            model.addAttribute("ticket", ticketService.loadTicketsByUser(principal.getName()));
            model.addAttribute("search", new Search());
            return "ticket/showList";
        }
    }

    @GetMapping({"/tickets/edit/{id}","/ticket/add"})
    public String showTicketForm(@PathVariable(name = "id", required = false) Integer id, Model model, Principal principal){
        model.addAttribute("ticket", ticketService.loadById(id));

        if(id == null || ticketService.exists(id) == false){
            model.addAttribute("user", userService.findUserByUsername(principal.getName()).getId());
            return "ticket/showAddForm";
        }
        return "ticket/showEditForm";
    }

    @PostMapping(value = {"/tickets/edit/{id}","/ticket/add"})
    public String processTicketForm(@Valid @ModelAttribute(name = "ticket") Ticket ticket, BindingResult bindingResult,
                                    @PathVariable(name = "id", required = false) Integer id, Model model,
                                    List<MultipartFile> multipartFile, Principal principal) throws IOException {
        if(bindingResult.hasErrors()){
            if(id == null){
                return "ticket/showAddForm";
            }
            return "ticket/showEditForm";
        }

        Integer idTicket = ticketService.save(ticket, multipartFile, principal.getName());

        model.addAttribute("ticket", ticketService.loadTicketById(idTicket));
        model.addAttribute("ticketReply", new TicketReply());
        model.addAttribute("status", new Status());
        return "ticket/showItem";

//        model.addAttribute("ticket", ticketService.loadAll());
//        model.addAttribute("search", new Search());
//        return "ticket/showList";
    }

    @Secured("ROLE_OPERATOR")
    @GetMapping("/tickets/{tID}/delete-image/{id}")
    public String deleteImage(@PathVariable(name = "tID", required = false) Integer ticketID,
            @PathVariable(name = "id", required = false) Integer imageID, Model model){
        imageService.delete(imageID);

        model.addAttribute("ticket", ticketService.loadById(ticketID));
        return "ticket/showEditForm";
    }

    @PostMapping("/ticket/reply/{id}")
    public String processAddReplyForm(@Valid @ModelAttribute(name = "ticketReply") TicketReply ticketReply, BindingResult bindingResult,
                                      @PathVariable(name = "id", required = false) Integer id, Model model, Principal principal) throws MessagingException {
        if(bindingResult.hasErrors()){
            model.addAttribute("ticket", ticketService.loadTicketById(id));
            return "ticket/showItem";
        }

        ticketReplyService.save(ticketReply, principal.getName(), id);

        model.addAttribute("ticket", ticketService.loadTicketById(id));
        model.addAttribute("ticketReply", new TicketReply());
        return "ticket/showItem";
    }

    @PostMapping("/tickets/status/{id}")
    public String changeTicketStatus(@PathVariable(name = "id", required = false) Integer id, Model model,
                                     @ModelAttribute(name = "status") Status status) throws MessagingException {

        ticketService.changeStatus(id, status);
        model.addAttribute("ticket", ticketService.loadAll());
        model.addAttribute("search", new Search());
        return "ticket/showList";
    }

    @Transactional
    @GetMapping("/tickets/delete/{id}")
    public String deleteTicket(@PathVariable(name = "id", required = false) Integer id, Model model, Principal principal){
        if(ticketService.isAuthorized(id,principal.getName())){
            ticketService.delete(id);
        }

        model.addAttribute("ticket", ticketService.loadAll());
        model.addAttribute("search", new Search());
        return "ticket/showList";
    }

    @Secured("ROLE_OPERATOR")
    @GetMapping("/tickets/{tID}/delete-reply/{id}")
    public String deleteReply(@PathVariable(name = "tID", required = false) Integer ticketID,
                              @PathVariable(name = "id", required = false) Integer replyID,
                              Model model){

        if(ticketService.exists(ticketID) && ticketReplyService.exists(replyID)){
            ticketReplyService.deleteById(replyID);

            model.addAttribute("ticket", ticketService.loadTicketById(ticketID));
            model.addAttribute("ticketReply", new TicketReply());
            model.addAttribute("status", new Status());

            return "ticket/showItem";
        }else{
            model.addAttribute("ticket", ticketService.loadAll());
            model.addAttribute("search", new Search());
            return "ticket/showList";
        }
    }

    @GetMapping(value = "/ticket/pdf/{id}")
    public ResponseEntity<InputStreamResource> getTermsConditions(@PathVariable(name = "id", required = false) Integer ticketID,
                                                                  Principal principal) throws IOException, DocumentException, com.lowagie.text.DocumentException {
        if(ticketService.isAuthorized(ticketID,principal.getName())) {
            String filePath = "src/main/resources/pdf/";
            String fileName = ticketID + ".pdf";
            File file = new File(filePath + fileName);
            HttpHeaders headers = new HttpHeaders();
            headers.add("content-disposition", "inline;filename=" + fileName);

            pdfService.createPDF(ticketID);
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("application/pdf"))
                    .body(resource);
        }
        else{
            throw new TicketNotFoundException();
        }

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

    @ModelAttribute("softwareList")
    public ArrayList<Software> loadSoftware(){
        return softwareService.loadAll();
    }

    @ModelAttribute("usersList")
    public ArrayList<User> loadUser(){
        return userService.loadAll();
    }

    @InitBinder
    public void initFormatters(WebDataBinder binder){
        binder.addCustomFormatter(new VersionFormatter());
    }

}
