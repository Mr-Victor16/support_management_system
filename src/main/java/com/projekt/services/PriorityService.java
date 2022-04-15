package com.projekt.services;

import com.projekt.models.Priority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public interface PriorityService {
    ArrayList<Priority> loadAll();

    Priority loadById(Integer id);

    boolean exists(Integer id);

    void save(Priority priority);

    void delete(Integer id);

    ArrayList<Integer> prioritiesUse();
}
