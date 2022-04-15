package com.projekt.services;

import com.projekt.models.*;
import com.projekt.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service("ticketDetailsService")
public class TicketServiceImpl implements TicketService{
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private TicketReplyService ticketReplyService;

    @Autowired
    private MailService mailService;

    @Override
    public ArrayList<Ticket> loadAll() {
        return (ArrayList<Ticket>) ticketRepository.findAll();
    }

    @Override
    public Ticket loadById(Integer id) {
        if(id == null || !ticketRepository.existsById(id)){
            return new Ticket();
        }

        return ticketRepository.getById(id);
    }

    @Override
    public boolean exists(Integer id) {
        return ticketRepository.existsById(id);
    }


    @Override
    public Integer save(Ticket ticket, List<MultipartFile> multipartFile, String name) throws IOException {
        ticket.setVersion(versionService.save(ticket.getVersion()));
        Version version = null;

        if(ticket.getTicketID() != null) {
            ticket.setTicketReplies(ticketRepository.getById(ticket.getTicketID()).getTicketReplies());
            version = ticketRepository.getById(ticket.getTicketID()).getVersion();

            if(multipartFile.isEmpty() || multipartFile.get(0).getOriginalFilename().isEmpty()){
                ticket.setImages(ticketRepository.getById(ticket.getTicketID()).getImages());
            }else{
                ticket.setImages(imageService.save(multipartFile,ticketRepository.getById(ticket.getTicketID()).getImages()));
            }
        }else{
            if(!multipartFile.isEmpty() && !multipartFile.get(0).getOriginalFilename().isEmpty()){
                ticket.setImages(imageService.save(multipartFile, null));
            }
        }

        if(ticket.getTicketDate() == null){
            ticket.setTicketDate(LocalDate.now());
        }
        if(ticket.getStatus() == null){
            ticket.setStatus(statusService.loadById(1));
        }
        if(ticket.getUser() == null){
            ticket.setUser(userService.findUserByUsername(name));
        }

        Ticket ticket1 = ticketRepository.save(ticket);

        if(version != null) {
            if (version.getVersionID() != ticket.getVersion().getVersionID()) {
                versionService.delete(version.getVersionID());
            }
        }

        return ticket1.getTicketID();
    }

    @Override
    public void delete(Integer id) {
        if(ticketRepository.existsById(id)){
            List<TicketReply> ticketReplyList = ticketRepository.getById(id).getTicketReplies();
            List<Image> imageList = ticketRepository.getById(id).getImages();

            ticketRepository.deleteByTicketID(id);
            ticketReplyService.deleteAll(ticketReplyList);
            imageService.deleteAll(imageList);
            versionService.delete(ticketRepository.getById(id).getVersion().getVersionID());
        }
    }

    @Override
    public boolean isAuthorized(Integer id, String name){
        if(ticketRepository.existsById(id)) {
            if(roleService.existsByIdAndUsername(2,name)){
                return true;
            }

            return (userService.findUserByUsername(name).getId() == ticketRepository.getById(id).getUser().getId());
        }
        return false;
    }

    @Override
    public Ticket loadTicketById(Integer id) {
        return ticketRepository.getById(id);
    }

    @Override
    public ArrayList<Ticket> loadTicketsByUser(String name) {
        return ticketRepository.findByUser_UsernameOrderByTicketDateAsc(name);
    }

    @Override
    public void addReply(TicketReply ticketReply, Integer id) throws MessagingException {
        Ticket ticket = ticketRepository.findById(id).get();
        ticket.getTicketReplies().add(ticketReply);

        if(ticket.getUser().getId() != ticketReply.getUser().getId()){
            mailService.sendTicketReplyMessage(ticket.getUser().getEmail(),ticket.getTicketTitle());
        }

        ticketRepository.save(ticket);
    }

    @Override
    public void changeStatus(Integer id, Status status) throws MessagingException {
        Ticket ticket = ticketRepository.getById(id);
        ticket.setStatus(status);
        mailService.sendChangeStatusMessage(ticket.getUser().getId(), ticket.getTicketTitle(), status.getStatusName());
        ticketRepository.save(ticket);
    }

    @Override
    public int countUseCategory(Integer id) {
        return ticketRepository.countByCategories_CategoryID(id);
    }

    @Override
    public int countUsePriority(Integer priorityID) {
        return ticketRepository.countByPriority_PriorityID(priorityID);
    }

    @Override
    public int countUseSoftware(Long softwareID) {
        return ticketRepository.countByVersion_Software_SoftwareID(softwareID);
    }

    @Override
    public int countUseStatus(Integer statusID) {
        return ticketRepository.countByStatus_StatusID(statusID);
    }

    @Override
    public void deleteByUserId(Integer id) {
        List<Ticket> tickets = ticketRepository.findByUser_Id(id);
        for(int i=0; i<tickets.size(); i++){
            imageService.deleteAll(tickets.get(i).getImages());
            ticketReplyService.deleteAll(tickets.get(i).getTicketReplies());
            ticketRepository.delete(tickets.get(i));
        }
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
        return ticketRepository.searchBySoftware(software.getSoftwareID());
    }

    @Override
    public ArrayList<Ticket> searchByStatus(Status status) {
        return ticketRepository.searchByStatus(status.getStatusID());
    }

    @Override
    public ArrayList<Ticket> searchByPriority(Priority priority) {
        return ticketRepository.searchByPriority(priority.getPriorityID());
    }

    @Override
    public ArrayList<Ticket> searchByVersion(Version version) {
        return ticketRepository.searchByVersion(version.toString());
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
