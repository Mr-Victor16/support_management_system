package com.projekt.services;

import com.projekt.models.*;
import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.add.AddTicketReply;
import com.projekt.payload.request.update.UpdateTicketRequest;
import com.projekt.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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
        if(userRepository.existsByUsernameIgnoreCaseAndRolesType(username, Role.Types.ROLE_OPERATOR)) return true;

        return (Objects.equals(userService.findUserByUsername(username).getId(), ticketRepository.getReferenceById(ticketID).getUser().getId()));
    }

    @Override
    public Ticket getById(Long id) {
        return ticketRepository.getReferenceById(id);
    }

    @Override
    public void addReply(AddTicketReply request) throws MessagingException {
        TicketReply ticketReply = new TicketReply();
        ticketReply.setDate(LocalDate.now());
        ticketReply.setUser(userRepository.getReferenceById(request.userID()));
        ticketReplyRepository.save(ticketReply);

        Ticket ticket = ticketRepository.getReferenceById(request.ticketID());
        ticket.getReplies().add(ticketReply);

        User user = ticketRepository.getReferenceById(request.ticketID()).getUser();

        if(!Objects.equals(user.getId(), request.userID())){
            mailService.sendTicketReplyMessage(user.getEmail(), ticket.getTitle());
        }

        ticketRepository.save(ticket);
    }

    @Override
    public void changeStatus(Long ticketID, Long statusID) throws MessagingException {
        Status status = statusRepository.getReferenceById(statusID);

        Ticket ticket = ticketRepository.getReferenceById(ticketID);
        ticket.setStatus(status);

        User user = ticketRepository.getReferenceById(ticketID).getUser();
        mailService.sendChangeStatusMessage(user.getId(), ticket.getTitle(), status.getName());

        ticketRepository.save(ticket);
    }

    @Override
    public Ticket findByImageId(Long imageID) {
        return ticketRepository.findByImagesId(imageID);
    }

    @Override
    public void add(AddTicketRequest request, String username) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());

        List<Image> images = request.multipartFiles().stream()
                .map(file -> {
                    try {
                        return new Image(file.getOriginalFilename(), file.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to process file", e);
                    }
                })
                .toList();
        ticket.setImages(images);

        ticket.setDate(LocalDate.now());
        ticket.setCategory(categoryRepository.getReferenceById(request.categoryID()));
        ticket.setPriority(priorityRepository.getReferenceById(request.priorityID()));
        ticket.setStatus(statusRepository.getReferenceById(request.statusID()));
        ticket.setVersion(request.version());
        ticket.setSoftware(softwareRepository.getReferenceById(request.softwareID()));
        ticket.setUser(userRepository.findByUsernameIgnoreCase(username));

        ticketRepository.save(ticket);
    }

    @Override
    public void update(UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.getReferenceById(request.ticketID());
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setCategory(categoryRepository.getReferenceById(request.categoryID()));
        ticket.setPriority(priorityRepository.getReferenceById(request.priorityID()));
        ticket.setVersion(request.version());
        ticket.setSoftware(softwareRepository.getReferenceById(request.softwareID()));

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
    public boolean existsByPriorityId(Long priorityID) {
        return ticketRepository.existsByPriorityId(priorityID);
    }

    @Override
    public boolean existsBySoftwareId(Long softwareID) {
        return ticketRepository.existsBySoftwareId(softwareID);
    }

    @Override
    public boolean existsByStatusId(Long statusID) {
        return ticketRepository.existsByStatusId(statusID);
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
