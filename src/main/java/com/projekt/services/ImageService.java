package com.projekt.services;

public interface ImageService {
    void deleteById(Long id);

    boolean existsById(Long imageID);
}
