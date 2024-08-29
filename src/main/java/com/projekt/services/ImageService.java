package com.projekt.services;

import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

public interface ImageService {
    void deleteById(Long id, Principal principal);

    boolean existsById(Long id);

    void add(Long ticketID, List<MultipartFile> files, Principal principal);
}
