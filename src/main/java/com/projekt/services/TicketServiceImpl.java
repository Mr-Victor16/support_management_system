package com.projekt.services;

import com.projekt.exceptions.*;
import com.projekt.models.*;
import com.projekt.payload.request.add.AddTicketRequest;
import com.projekt.payload.request.update.UpdateTicketRequest;
import com.projekt.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service("ticketDetailsService")
public class TicketServiceImpl implements TicketService{
    private final TicketRepository ticketRepository;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    private final CategoryRepository categoryRepository;
    private final PriorityRepository priorityRepository;
    private final SoftwareRepository softwareRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, MailService mailService, UserRepository userRepository, StatusRepository statusRepository, CategoryRepository categoryRepository, PriorityRepository priorityRepository, SoftwareRepository softwareRepository) {
        this.ticketRepository = ticketRepository;
        this.mailService = mailService;
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
    public void delete(Long id, Principal principal) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket", id));

        if(!isAuthorized(ticket.getId(), principal.getName())){
            throw UnauthorizedActionException.forActionOnResource("delete", "ticket");
        }

        ticketRepository.deleteById(ticket.getId());
    }

    @Override
    public boolean isAuthorized(Long ticketID, String username){
        if(userRepository.existsByUsernameIgnoreCaseAndRolesType(username, Role.Types.ROLE_OPERATOR)) return true;

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User", username));

        Ticket ticket = ticketRepository.findById(ticketID)
                .orElseThrow(() -> new NotFoundException("Ticket", ticketID));

        return Objects.equals(user.getId(), ticket.getUser().getId());
    }

    @Override
    public List<Ticket> findUserTickets(Principal principal) {
        User user = userRepository.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new NotFoundException("User", principal.getName()));

        return user.getTickets();
    }

    @Override
    public Ticket getById(Long id, Principal principal) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket", id));

        if(!isAuthorized(ticket.getId(), principal.getName())){
            throw UnauthorizedActionException.forActionToResource("access", "ticket");
        }

        return ticket;
    }

    @Override
    public void changeStatus(Long ticketID, Long statusID) {
        Ticket ticket = ticketRepository.findById(ticketID)
                .orElseThrow(() -> new NotFoundException("Ticket", ticketID));

        Status status = statusRepository.findById(statusID)
                .orElseThrow(() -> new NotFoundException("Status", statusID));

        ticket.setStatus(status);
        ticketRepository.save(ticket);

        try {
            mailService.sendChangeStatusMessage(ticket.getUser().getId(), ticket.getTitle(), status.getName());
        } catch (MessagingException ex) {
            throw new NotificationFailedException("Error occurred while sending notification", ex);
        }
    }

    @Override
    public void add(AddTicketRequest request, String username) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());

        List<Image> images = processFiles(request.multipartFiles());
        ticket.setImages(images);

        ticket.setDate(LocalDate.now());

        ticket.setCategory(categoryRepository.findById(request.categoryID())
                .orElseThrow(() -> new NotFoundException("Category", request.categoryID())));

        ticket.setPriority(priorityRepository.findById(request.priorityID())
                .orElseThrow(() -> new NotFoundException("Priority", request.priorityID())));

        ticket.setStatus(statusRepository.findById(request.statusID())
                .orElseThrow(() -> new NotFoundException("Status", request.statusID())));

        ticket.setVersion(request.version());

        ticket.setSoftware(softwareRepository.findById(request.softwareID())
                .orElseThrow(() -> new NotFoundException("Software", request.softwareID())));

        ticket.setUser(userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User", username))
        );

        ticketRepository.save(ticket);
    }

    @Override
    public List<Image> processFiles(List<MultipartFile> files) {
        return files.stream()
                .map(file -> {
                    try {
                        return new Image(file.getOriginalFilename(), file.getBytes());
                    } catch (IOException ex) {
                        throw new FileProcessingException(file.getOriginalFilename(), ex.getCause());
                    }
                })
                .toList();
    }

    @Override
    public void update(UpdateTicketRequest request, Principal principal) {
        Ticket ticket = ticketRepository.findById(request.ticketID())
                .orElseThrow(() -> new NotFoundException("Ticket", request.ticketID()));

        if(!isAuthorized(ticket.getId(), principal.getName())){
            throw UnauthorizedActionException.forActionOnResource("update", "ticket");
        }

        ticket.setTitle(request.title());
        ticket.setDescription(request.description());

        ticket.setCategory(categoryRepository.findById(request.categoryID())
                .orElseThrow(() -> new NotFoundException("Category", request.categoryID())));
        ticket.setPriority(priorityRepository.findById(request.priorityID())
                .orElseThrow(() -> new NotFoundException("Priority", request.priorityID())));
        ticket.setSoftware(softwareRepository.findById(request.softwareID())
                .orElseThrow(() -> new NotFoundException("Software", request.softwareID())));

        ticket.setVersion(request.version());

        ticketRepository.save(ticket);
    }
}
