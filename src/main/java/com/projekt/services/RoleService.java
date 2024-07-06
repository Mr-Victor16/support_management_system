package com.projekt.services;

import com.projekt.models.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleService {
    List<Role> getAll();

    Role getById(Long id);
}
