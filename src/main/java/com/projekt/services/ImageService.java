package com.projekt.services;

import org.springframework.stereotype.Service;

@Service
public interface ImageService {
    void deleteById(Long id);

    boolean existsById(Long imageID);
}
