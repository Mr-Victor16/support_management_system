package com.projekt.services;

import com.projekt.exceptions.FileProcessingException;
import com.projekt.exceptions.NotFoundException;
import com.projekt.exceptions.UnauthorizedActionException;
import com.projekt.models.Image;
import com.projekt.models.Ticket;
import com.projekt.repositories.ImageRepository;
import com.projekt.repositories.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service("imageService")
public class ImageServiceImpl implements ImageService{
    private final ImageRepository imageRepository;
    private final TicketService ticketService;
    private final TicketRepository ticketRepository;

    public ImageServiceImpl(ImageRepository imageRepository, TicketService ticketService, TicketRepository ticketRepository) {
        this.imageRepository = imageRepository;
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void deleteById(Long id, Principal principal) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image", id));

        if(!ticketService.isAuthorized(ticketRepository.findByImagesId(id).getId(), principal.getName())){
            throw UnauthorizedActionException.forActionOnResource("delete", "image");
        }

        imageRepository.deleteById(image.getId());
    }

    @Override
    public boolean existsById(Long id) {
        return imageRepository.existsById(id);
    }

    @Override
    public void add(Long ticketID, List<MultipartFile> files, Principal principal){
        Ticket ticket = ticketRepository.findById(ticketID)
                .orElseThrow(() -> new NotFoundException("Ticket", ticketID));

        if(!ticketService.isAuthorized(ticket.getId(), principal.getName())){
            throw UnauthorizedActionException.forActionToResource("add image", "ticket");
        }

        List<Image> images = processFiles(files);
        ticket.getImages().addAll(images);

        ticketRepository.save(ticket);
    }

    private List<Image> processFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return new ArrayList<>();

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
}
