package com.projekt.services;

import com.projekt.models.*;
import com.projekt.repositories.TicketReplyRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service("ticketDetailsService")
public class TicketServiceImpl implements TicketService{
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final RoleService roleService;
    private final ImageService imageService;
    private final StatusService statusService;
    private final MailService mailService;
    private final TicketReplyRepository ticketReplyRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, UserService userService, RoleService roleService, ImageService imageService, StatusService statusService, MailService mailService, TicketReplyRepository ticketReplyRepository) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.roleService = roleService;
        this.imageService = imageService;
        this.statusService = statusService;
        this.mailService = mailService;
        this.ticketReplyRepository = ticketReplyRepository;
    }

    @Override
    public ArrayList<Ticket> loadAll() {
        return (ArrayList<Ticket>) ticketRepository.findAll();
    }

    @Override
    public Ticket loadById(Integer id) {
        if(id == null || !ticketRepository.existsById(id)){
            return new Ticket();
        }

        return ticketRepository.getReferenceById(id);
    }

    @Override
    public boolean exists(Integer id) {
        return ticketRepository.existsById(id);
    }


    @Override
    public Integer save(Ticket ticket, List<MultipartFile> multipartFile, String name) throws IOException {
        if(ticket.getId() != null) {
            ticket.setTicketReplies(ticketRepository.getReferenceById(ticket.getId()).getTicketReplies());

            if(multipartFile.isEmpty() || multipartFile.get(0).getOriginalFilename().isEmpty()){
                ticket.setImages(ticketRepository.getReferenceById(ticket.getId()).getImages());
            }else{
                ticket.setImages(imageService.save(multipartFile,ticketRepository.getReferenceById(ticket.getId()).getImages()));
            }
        }else{
            if(!multipartFile.isEmpty() && !multipartFile.get(0).getOriginalFilename().isEmpty()){
                ticket.setImages(imageService.save(multipartFile, null));
            }
        }

        if(ticket.getDate() == null){
            ticket.setDate(LocalDate.now());
        }
        if(ticket.getStatus() == null){
            ticket.setStatus(statusService.loadById(1));
        }
//        if(ticket.getUser() == null){
//            ticket.setUser(userService.findUserByUsername(name));
//        }

        Ticket ticket1 = ticketRepository.save(ticket);

        return ticket1.getId();
    }

    @Override
    public void delete(Integer id) {
        if(ticketRepository.existsById(id)){
            List<TicketReply> ticketReplyList = ticketRepository.getReferenceById(id).getTicketReplies();
            List<Image> imageList = ticketRepository.getReferenceById(id).getImages();

            ticketRepository.deleteById(id);
            ticketReplyRepository.deleteAll(ticketReplyList);
            imageService.deleteAll(imageList);
        }
    }

    @Override
    public boolean isAuthorized(Integer id, String name){
        if(ticketRepository.existsById(id)) {
            if(roleService.existsByIdAndUsername(2,name)){
                return true;
            }

            //return (userService.findUserByUsername(name).getId() == ticketRepository.getReferenceById(id).getUser().getId());
        }
        return false;
    }

    @Override
    public Ticket loadTicketById(Integer id) {
        return ticketRepository.getReferenceById(id);
    }

    @Override
    public ArrayList<Ticket> loadTicketsByUser(String name) {
        return ticketRepository.findByUser_UsernameOrderByDateAsc(name);
    }

    @Override
    public void addReply(TicketReply ticketReply, Integer id) throws MessagingException {
        Ticket ticket = ticketRepository.findById(id).get();
        ticket.getTicketReplies().add(ticketReply);

//        if(ticket.getUser().getId() != ticketReply.getUser().getId()){
//            mailService.sendTicketReplyMessage(ticket.getUser().getEmail(),ticket.getTitle());
//        }

        ticketRepository.save(ticket);
    }

    @Override
    public void changeStatus(Integer id, Status status) throws MessagingException {
        Ticket ticket = ticketRepository.getReferenceById(id);
        ticket.setStatus(status);
//        mailService.sendChangeStatusMessage(ticket.getUser().getId(), ticket.getTitle(), status.getName());
        ticketRepository.save(ticket);
    }

    @Override
    public ArrayList<Ticket> searchByPhrase(String phrase) {
        return ticketRepository.searchByPhrase(phrase);
    }

    @Override
    public ArrayList<Ticket> searchByDate(LocalDate date1, LocalDate date2) {
        return ticketRepository.searchByDate(date1,date2);
    }

    @Override
    public ArrayList<Ticket> searchBySoftware(Software software) {
        return ticketRepository.searchBySoftware(software.getId());
    }

    @Override
    public ArrayList<Ticket> searchByStatus(Status status) {
        return ticketRepository.searchByStatus(status.getId());
    }

    @Override
    public ArrayList<Ticket> searchByPriority(Priority priority) {
        return ticketRepository.searchByPriority(priority.getId());
    }

    @Override
    public ArrayList<Ticket> searchByVersion(String version) {
        return ticketRepository.searchByVersion(version);
    }

    @Override
    public ArrayList<Ticket> searchByCategory(Set<Category> categories) {
        return ticketRepository.findDistinctByCategoriesIn(categories);
    }

    @Override
    public ArrayList<Ticket> searchByReplyNumber(int number1, int number2) {
        return ticketRepository.searchByReplyNumber(number1, number2);
    }

}
