package com.projekt.services;

import com.projekt.models.Priority;
import com.projekt.payload.request.add.AddPriorityRequest;
import com.projekt.payload.request.edit.EditPriorityRequest;
import com.projekt.payload.response.PriorityResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PriorityService {
    Priority loadById(Long id);

    void save(AddPriorityRequest priorityRequest);

    void delete(Long id);

    List<Priority> getAll();

    List<PriorityResponse> getAllWithUseNumber();

    boolean existsById(Long priorityID);

    boolean existsByName(String priorityName);

    void update(EditPriorityRequest priorityRequest);
}
