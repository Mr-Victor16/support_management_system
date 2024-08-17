package com.projekt.services;

import com.projekt.models.Role;
import com.projekt.models.User;
import com.projekt.payload.request.*;
import com.projekt.payload.request.add.AddUserRequest;
import com.projekt.payload.request.edit.EditProfileDetailsRequest;
import com.projekt.payload.request.edit.EditUserRequest;
import com.projekt.payload.response.LoginResponse;
import com.projekt.payload.response.UserDetailsResponse;
import com.projekt.repositories.RoleRepository;
import com.projekt.repositories.UserRepository;
import com.projekt.security.jwt.JWTUtils;
import com.projekt.security.services.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service("userDetailsService")
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final MailService mailService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(UserRepository userRepository, MailService mailService, RoleRepository roleRepository,
                           PasswordEncoder encoder, JWTUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsernameIgnoreCase(username);
        if (user == null) throw new UsernameNotFoundException(username);

        return convertToUserDetails(user);
    }

    @Override
    public void editUser(EditUserRequest request) throws Exception {
        User user = userRepository.getReferenceById(request.getId());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email is already in use");
        }

        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        Set<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findRoleByType(Role.Types.valueOf(roleName))
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        user.setEnabled(request.isEnabled());

        userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void activate(Long userID) {
        User user = userRepository.getReferenceById(userID);
        user.setEnabled(true);

        userRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameIgnoreCase(username.toLowerCase());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    @Override
    public void register(RegisterRequest request) throws MessagingException {
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());

        newUser.setPassword(encoder.encode(request.getPassword()));
        newUser.setName(request.getName());
        newUser.setSurname(request.getSurname());

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByType(Role.Types.ROLE_USER));
        newUser.setRoles(roles);

        User user = userRepository.save(newUser);
        mailService.sendRegisterMessage(user.getId(), user.isEnabled());
    }

    @Override
    public LoginResponse authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new LoginResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getName(),
                userDetails.getSurname(),
                userDetails.getEmail(),
                token,
                roles
        );
    }

    @Override
    public UserDetailsResponse getUserDetails(String name) {
        return convertToUserDetailsResponse(userRepository.findByUsernameIgnoreCase(name));
    }

    @Override
    public void updateProfile(String username, EditProfileDetailsRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(username);
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setPassword(encoder.encode(request.getPassword()));

        userRepository.save(user);
    }

    @Override
    public boolean isActive(Long userID) {
        return userRepository.getReferenceById(userID).isEnabled();
    }

    @Override
    public void addUser(AddUserRequest request) {
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        Set<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findRoleByType(Role.Types.valueOf(roleName))
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        user.setEnabled(true);

        userRepository.save(user);
    }

    @Override
    public User findUserByUsername(String name) {
        return userRepository.findByUsernameIgnoreCase(name);
    }

    @Override
    public List<UserDetailsResponse> loadAll() {
        return userRepository.findAll().stream()
                .map(user -> convertToUserDetailsResponse(user))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public UserDetailsResponse loadById(Long id) {
        return convertToUserDetailsResponse(userRepository.getReferenceById(id));
    }

    private UserDetails convertToUserDetails(User user) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : user.getRoles()){
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getType().toString()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                grantedAuthorities
        );
    }

    private UserDetailsResponse convertToUserDetailsResponse(User user){
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getType().name())
                .toList();

        return new UserDetailsResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                roles
        );
    }
}
