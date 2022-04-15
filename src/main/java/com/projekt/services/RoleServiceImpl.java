package com.projekt.services;

import com.projekt.models.Role;
import com.projekt.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service("roleService")
public class RoleServiceImpl implements RoleService{
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public ArrayList<Role> loadAll() {
        return (ArrayList<Role>) roleRepository.findAll();
    }

    @Override
    public Role loadById(int id) {
        return roleRepository.getById(id);
    }

    @Override
    public boolean existsByIdAndUsername(Integer idRole, String username) {
        return roleRepository.existsByIdAndUsers_Username(idRole,username);
    }

}
