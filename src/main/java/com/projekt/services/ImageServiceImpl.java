package com.projekt.services;

import com.projekt.repositories.ImageRepository;
import org.springframework.stereotype.Service;

@Service("imageService")
public class ImageServiceImpl implements ImageService{
    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public void deleteById(Long id) {
        imageRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long imageID) {
        return imageRepository.existsById(imageID);
    }
}
