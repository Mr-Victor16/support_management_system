package com.projekt.services;

import com.projekt.models.Status;
import com.projekt.payload.request.AddStatusRequest;
import com.projekt.payload.request.EditStatusRequest;
import com.projekt.payload.response.StatusResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StatusService {
    Status loadById(Long id);

    boolean existsById(Long id);

    void delete(Long id);

    void save(AddStatusRequest statusRequest);

    List<Status> getAll();

    List<StatusResponse> getAllWithUseNumber();

    boolean existsByName(String statusName);

    void update(EditStatusRequest statusRequest);
}
