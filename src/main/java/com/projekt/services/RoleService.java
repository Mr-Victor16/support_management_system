package com.projekt.services;

import com.projekt.models.Role;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public interface RoleService {
    ArrayList<Role> loadAll();

    Role loadById(int i);

    boolean existsByIdAndUsername(Integer idRole, String username);
}
