package com.projekt.services;

import com.projekt.models.Status;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public interface StatusService {
    ArrayList<Status> loadAll();

    Status loadById(Integer id);

    boolean exists(Integer id);

    void delete(Integer id);

    void save(Status status);

    ArrayList<Integer> statusUse();
}
