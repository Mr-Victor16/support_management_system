package com.projekt.services;

import com.projekt.models.*;
import com.projekt.payload.request.AddTicketReply;
import com.projekt.payload.request.AddTicketRequest;
import com.projekt.payload.request.EditTicketRequest;
import com.projekt.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service("ticketDetailsService")
public class TicketServiceImpl implements TicketService{
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final MailService mailService;
    private final TicketReplyRepository ticketReplyRepository;
    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    private final CategoryRepository categoryRepository;
    private final PriorityRepository priorityRepository;
    private final SoftwareRepository softwareRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, UserService userService, MailService mailService, TicketReplyRepository ticketReplyRepository, UserRepository userRepository, StatusRepository statusRepository, CategoryRepository categoryRepository, PriorityRepository priorityRepository, SoftwareRepository softwareRepository) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.ticketReplyRepository = ticketReplyRepository;
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
        this.categoryRepository = categoryRepository;
        this.priorityRepository = priorityRepository;
        this.softwareRepository = softwareRepository;
    }

    @Override
    public List<Ticket> getAll() {
        return ticketRepository.findAll();
    }

    @Override
    public boolean existsById(Long id) {
        return ticketRepository.existsById(id);
    }

    @Override
    public void delete(Long id) {
        ticketRepository.deleteById(id);
    }

    @Override
    public boolean isAuthorized(Long ticketID, String username){
        if(userRepository.existsByUsernameAndRolesType(username, Role.Types.ROLE_OPERATOR)) return true;

        return (userService.findUserByUsername(username).getId() == userRepository.findByTicketsId(ticketID).getId());
    }

    @Override
    public Ticket getById(Long id) {
        return ticketRepository.getReferenceById(id);
    }

    @Override
    public List<Ticket> getTicketsByUserId(Long id) {
        return userRepository.getReferenceById(id).getTickets();
    }

    @Override
    public void addReply(AddTicketReply request) throws MessagingException {
        TicketReply ticketReply = new TicketReply();
        ticketReply.setDate(LocalDate.now());
        ticketReply.setUser(userRepository.getReferenceById(request.getUserID()));
        ticketReplyRepository.save(ticketReply);

        Ticket ticket = ticketRepository.getReferenceById(request.getTicketID());
        ticket.getTicketReplies().add(ticketReply);

        User user = userRepository.findByTicketsId(request.getTicketID());

        if(user.getId() != request.getUserID()){
            mailService.sendTicketReplyMessage(user.getEmail(), ticket.getTitle());
        }

        ticketRepository.save(ticket);
    }

    @Override
    public void changeStatus(Long ticketID, Long statusID) throws MessagingException {
        Status status = statusRepository.getReferenceById(statusID);

        Ticket ticket = ticketRepository.getReferenceById(ticketID);
        ticket.setStatus(status);

        User user = userRepository.findByTicketsId(ticketID);
        mailService.sendChangeStatusMessage(user.getId(), ticket.getTitle(), status.getName());

        ticketRepository.save(ticket);
    }

    @Override
    public Ticket findByImageId(Long imageID) {
        return ticketRepository.findByImagesImageID(imageID);
    }

    @Override
    public void add(AddTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());

        List<Image> images = request.getMultipartFiles().stream()
                .map(file -> {
                    try {
                        return new Image(file.getOriginalFilename(), file.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to process file", e);
                    }
                })
                .collect(Collectors.toList());
        ticket.setImages(images);

        ticket.setDate(LocalDate.now());
        ticket.setCategory(categoryRepository.getReferenceById(request.getCategoryID()));
        ticket.setPriority(priorityRepository.getReferenceById(request.getPriorityID()));
        ticket.setStatus(statusRepository.getReferenceById(request.getStatusID()));
        ticket.setVersion(request.getVersion());
        ticket.setSoftware(softwareRepository.getReferenceById(request.getSoftwareID()));

        ticketRepository.save(ticket);
    }

    @Override
    public void update(EditTicketRequest request) {
        Ticket ticket = ticketRepository.getReferenceById(request.getTicketID());
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(categoryRepository.getReferenceById(request.getCategoryID()));
        ticket.setPriority(priorityRepository.getReferenceById(request.getPriorityID()));
        ticket.setVersion(request.getVersion());
        ticket.setSoftware(softwareRepository.getReferenceById(request.getSoftwareID()));

        ticketRepository.save(ticket);
    }

    @Override
    public void addImage(Long ticketID, MultipartFile file) throws IOException {
        Ticket ticket = ticketRepository.getReferenceById(ticketID);
        ticket.getImages().add(new Image(file.getOriginalFilename(), file.getBytes()));

        ticketRepository.save(ticket);
    }

    @Override
    public boolean existsByCategoryId(Long categoryID) {
        return ticketRepository.existsByCategoryId(categoryID);
    }

    @Override
    public boolean entitiesExist(Long categoryID, Long statusID, Long priorityID, Long softwareID) {
        boolean categoryExists = categoryRepository.existsById(categoryID);
        boolean statusExists = statusRepository.existsById(statusID);
        boolean priorityExists = priorityRepository.existsById(priorityID);
        boolean softwareExists = softwareRepository.existsById(softwareID);

        return categoryExists && priorityExists && statusExists && softwareExists;
    }
}
