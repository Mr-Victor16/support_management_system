package com.projekt.services;

import com.projekt.models.Role;
import com.projekt.payload.request.*;
import com.projekt.payload.response.LoginResponse;
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
import org.springframework.security.core.userdetails.User;
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
        var user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return convertToUserDetails(user);
    }

    @Override
    public void editUser(EditUserRequest request) throws Exception {
        com.projekt.models.User user = userRepository.getReferenceById(request.getId());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email is already in use");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new Exception("Username is already in use");
        }

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
        com.projekt.models.User user = userRepository.getReferenceById(userID);
        user.setEnabled(true);

        userRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username.toLowerCase());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    @Override
    public void register(RegisterRequest request) throws MessagingException {
        com.projekt.models.User newUser = new com.projekt.models.User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());

        newUser.setPassword(encoder.encode(request.getPassword()));
        newUser.setName(request.getName());
        newUser.setSurname(request.getSurname());

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByType(Role.Types.ROLE_USER));
        newUser.setRoles(roles);

        com.projekt.models.User user = userRepository.save(newUser);
        mailService.sendRegisterMessage(user.getId(), user.isEnabled());
    }

    @Override
    public LoginResponse authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

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
    public com.projekt.payload.response.UserDetails getUserDetails(String name) {
        com.projekt.models.User user = userRepository.findByUsername(name);
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getType().name())
                .toList();

        return new com.projekt.payload.response.UserDetails(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                roles
        );
    }

    @Override
    public void updateProfile(ProfileDetailsRequest request) {
        com.projekt.models.User user = userRepository.getReferenceById(request.getId());
        user.setName(request.getName());
        user.setSurname(request.getSurname());

        userRepository.save(user);
    }

    @Override
    public boolean isActive(Long userID) {
        return userRepository.getReferenceById(userID).isEnabled();
    }

    @Override
    public void addUser(AddUserRequest request) {
        com.projekt.models.User user = new com.projekt.models.User();

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
    public com.projekt.models.User findUserByUsername(String name) {
        return userRepository.findByUsername(name);
    }

    @Override
    public List<com.projekt.payload.response.UserDetails> loadAll() {
        return userRepository.findAll().stream()
                .map(user -> new com.projekt.payload.response.UserDetails(
                        user.getId(),
                        user.getUsername(),
                        user.getName(),
                        user.getSurname(),
                        user.getEmail(),
                        user.getRoles().stream()
                                .map(role -> role.getType().name())
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public com.projekt.payload.response.UserDetails loadById(Long id) {
        com.projekt.models.User user = userRepository.getReferenceById(id);
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getType().name())
                .toList();

        return new com.projekt.payload.response.UserDetails(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                roles
        );
    }

    private UserDetails convertToUserDetails(com.projekt.models.User user) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : user.getRoles()){
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getType().toString()));
        }

        return new User(user.getUsername(), user.getPassword(), user.isEnabled(), true, true, true, grantedAuthorities);
    }
}
