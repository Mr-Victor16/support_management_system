package com.projekt.services;

import com.projekt.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.util.ArrayList;

@Service
public interface UserService extends UserDetailsService {
    void saveUser(User user, boolean mail, boolean admin, boolean enabled) throws MessagingException;

    User findUserByUsername(String name);

    ArrayList<User> loadAll();

    boolean exists(Integer id);

    User loadById(Integer id);

    void editUser(User user);

    boolean permit(Integer id, String name);

    void delete(Integer id);

    ArrayList<User> searchUserByNameSurnameUsername(String phrase);

    ArrayList<User> searchUserByEmail(String email);

    ArrayList<User> searchUserByRole(Integer id);

    boolean activate(Integer userID);
}
