package com.projekt.services;

import com.projekt.config.ProfileNames;
import com.projekt.models.Role;
import com.projekt.repositories.RoleRepository;
import com.projekt.repositories.TicketRepository;
import com.projekt.repositories.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service("userDetailsService")
@Profile(ProfileNames.USERS_IN_DATABASE)
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final MailService mailService;
    private final RoleRepository roleRepository;
    private final TicketRepository ticketRepository;

    public UserServiceImpl(UserRepository userRepository, MailService mailService, RoleRepository roleRepository, TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.roleRepository = roleRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return convertToUserDetails(user);
    }

    @Override
    public void editUser(com.projekt.models.User user){
        if((userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail()) == null) ||
                (userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail()).getId() == user.getId())) {

            if(user.getPassword().equals("-")){
                com.projekt.models.User user1 = userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail());
                user.setPassword(user1.getPassword());
            }else {
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            user.setEnabled(true);

            if(user.getRoles() == null){
                Set<Role> role = userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail()).getRoles();
                user.setRoles(role);
            }

            userRepository.save(setRole(user));
        }else{
            throw new RuntimeException();
        }
    }

    @Override
    public boolean permit(Integer id, String name) {
        return (findUserByUsername(name).getId() == id);
    }

    @Override
    public void delete(Integer id) {
        if(userRepository.existsById(id)){
            ticketRepository.deleteByUserId(id);
            userRepository.deleteById(id);
        }
    }

    @Override
    public ArrayList<com.projekt.models.User> searchUserByNameSurnameUsername(String phrase) {
        return userRepository.searchUserByNameSurnameUsername(phrase);
    }

    @Override
    public ArrayList<com.projekt.models.User> searchUserByEmail(String email) {
        return userRepository.searchUserByEmail(email);
    }

    @Override
    public ArrayList<com.projekt.models.User> searchUserByRole(Integer id) {
        ArrayList<com.projekt.models.User> list = userRepository.findByRoles_Id(id);
        ArrayList<com.projekt.models.User> list2 = new ArrayList<>();

        for(int i=0; i<list.size(); i++){
            Set<Role> set = list.get(i).getRoles();
            if((set.size() == 1 && id == 1) || (set.size() == 2 && id == 2) || (set.size()==3 && id==3)){
                list2.add(list.get(i));
            }
        }

        return list2;
    }

    @Override
    public boolean activate(Integer userID) {
        if(!userRepository.getReferenceById(userID).isEnabled()){
            com.projekt.models.User user = userRepository.getReferenceById(userID);
            user.setEnabled(true);
            userRepository.save(user);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void saveUser(com.projekt.models.User user, boolean mail, boolean admin, boolean enabled) throws MessagingException {
        if(userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail()) == null){
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setEnabled(enabled);

            if(!admin) {
                Set<Role> roleSet = new HashSet<>();
                roleSet.add(roleRepository.getReferenceById(1));
                user.setRoles(roleSet);
            }

            userRepository.save(setRole(user));

            if(mail){
                mailService.sendRegisterMessage(user.getEmail(),user.getUsername(),enabled);
            }
        }else{
            throw new RuntimeException();
        }
    }

    private com.projekt.models.User setRole(com.projekt.models.User user){
        Set<Role> roleList = user.getRoles();
        if (roleList.size() == 1){
            if(roleList.stream().findFirst().get().getType().toString() == "ROLE_ADMIN"){
                roleList.add(roleRepository.getReferenceById(1));
                roleList.add(roleRepository.getReferenceById(2));
            }else if(roleList.stream().findFirst().get().getType().toString() == "ROLE_OPERATOR"){
                roleList.add(roleRepository.getReferenceById(1));
            }

            user.setRoles(roleList);
        }

        return user;
    }

    @Override
    public com.projekt.models.User findUserByUsername(String name) {
        return userRepository.findByUsername(name);
    }

    @Override
    public ArrayList<com.projekt.models.User> loadAll() {
        return (ArrayList<com.projekt.models.User>) userRepository.findAll();
    }

    @Override
    public boolean exists(Integer id) {
        return userRepository.existsById(id);
    }

    @Override
    public com.projekt.models.User loadById(Integer id) {
        if(id == null || !userRepository.existsById(id)){
            return new com.projekt.models.User();
        }

        return userRepository.getReferenceById(id);
    }

    private UserDetails convertToUserDetails(com.projekt.models.User user) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : user.getRoles()){
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getType().toString()));
        }

        return new User(user.getUsername(), user.getPassword(), user.isEnabled(), true, true, true, grantedAuthorities);
    }

}

