package com.projekt.services;

import com.projekt.models.Software;
import com.projekt.payload.request.EditSoftwareRequest;
import com.projekt.payload.request.AddSoftwareRequest;
import com.projekt.payload.response.SoftwareResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SoftwareService {
    List<Software> getAll();

    Software loadById(Long id);

    boolean existsById(Long id);

    void delete(Long id);

    void save(AddSoftwareRequest software);

    List<SoftwareResponse> getAllWithUseNumber();

    void update(EditSoftwareRequest request);

    boolean existsByName(String softwareName);
}
