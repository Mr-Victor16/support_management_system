package com.projekt.services;

import com.projekt.models.Status;
import com.projekt.payload.request.add.AddStatusRequest;
import com.projekt.payload.request.update.UpdateStatusRequest;
import com.projekt.payload.response.StatusResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StatusService {
    Status loadById(Long id);

    boolean existsById(Long id);

    void delete(Long id);

    void add(AddStatusRequest request);

    List<Status> getAll();

    List<StatusResponse> getAllWithUseNumber();

    void update(UpdateStatusRequest request);
}
