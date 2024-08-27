package com.projekt.services;

import com.projekt.models.Software;
import com.projekt.payload.request.add.AddSoftwareRequest;
import com.projekt.payload.request.update.UpdateSoftwareRequest;
import com.projekt.payload.response.SoftwareResponse;

import java.util.List;

public interface SoftwareService {
    List<Software> getAll();

    Software loadById(Long id);

    boolean existsById(Long id);

    void delete(Long id);

    void save(AddSoftwareRequest software);

    List<SoftwareResponse> getAllWithUseNumber();

    void update(UpdateSoftwareRequest request);

    boolean existsByName(String softwareName);
}
