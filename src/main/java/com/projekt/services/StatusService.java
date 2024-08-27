package com.projekt.services;

import com.projekt.models.Status;
import com.projekt.payload.request.add.AddStatusRequest;
import com.projekt.payload.request.update.UpdateStatusRequest;
import com.projekt.payload.response.StatusResponse;

import java.util.List;

public interface StatusService {
    Status loadById(Long id);

    boolean existsById(Long id);

    void delete(Long id);

    void save(AddStatusRequest statusRequest);

    List<Status> getAll();

    List<StatusResponse> getAllWithUseNumber();

    boolean existsByName(String statusName);

    void update(UpdateStatusRequest statusRequest);
}
