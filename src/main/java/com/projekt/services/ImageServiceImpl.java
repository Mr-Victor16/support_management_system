package com.projekt.services;

import com.projekt.models.Image;
import com.projekt.repositories.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service("imageService")
public class ImageServiceImpl implements ImageService{
    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public void delete(Integer id) {
        imageRepository.deleteById(id);
    }

    @Override
    public List<Image> save(List<MultipartFile> multipartFile, List<Image> images) throws IOException {
        if(images == null){
            images = new ArrayList<>();
        }

        for(int i=0; i< multipartFile.size(); i++) {
            Image image = new Image();
            image.setFileName(multipartFile.get(i).getOriginalFilename());
            image.setFileContent(multipartFile.get(i).getBytes());
            imageRepository.save(image);
            images.add(image);
        }

        return images;
    }

    @Override
    public void deleteAll(List<Image> images) {
        for(int i=0; i<images.size(); i++) {
            imageRepository.deleteById(images.get(i).getImageID());
        }
    }

}
