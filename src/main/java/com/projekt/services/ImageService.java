package com.projekt.services;

import com.projekt.models.Image;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface ImageService {
    void delete(Integer id);

    List<Image> save(List<MultipartFile> multipartFile, List<Image> images) throws IOException;

    void deleteAll(List<Image> images);
}
