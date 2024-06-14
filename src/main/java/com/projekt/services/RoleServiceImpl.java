package com.projekt.services;

import com.projekt.models.Role;
import com.projekt.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service("roleService")
public class RoleServiceImpl implements RoleService{
    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public ArrayList<Role> loadAll() {
        return (ArrayList<Role>) roleRepository.findAll();
    }

    @Override
    public Role loadById(int id) {
        return roleRepository.getReferenceById(id);
    }

    @Override
    public boolean existsByIdAndUsername(Integer idRole, String username) {
        return roleRepository.existsByIdAndUsers_Username(idRole,username);
    }

}
