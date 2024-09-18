package com.projekt.services;

import com.projekt.converter.UserConverter;
import com.projekt.exceptions.*;
import com.projekt.models.Role;
import com.projekt.models.User;
import com.projekt.payload.request.*;
import com.projekt.payload.request.add.AddUserRequest;
import com.projekt.payload.request.update.UpdateProfileDetailsRequest;
import com.projekt.payload.request.update.UpdateUserRequest;
import com.projekt.payload.response.LoginResponse;
import com.projekt.payload.response.UserDetailsResponse;
import com.projekt.repositories.RoleRepository;
import com.projekt.repositories.UserRepository;
import com.projekt.security.jwt.JWTUtils;
import com.projekt.security.services.UserDetailsImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;

import java.util.List;
import java.util.Set;

@Service("userDetailsService")
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final MailService mailService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserConverter userConverter;

    public UserServiceImpl(UserRepository userRepository, MailService mailService, RoleRepository roleRepository,
                           PasswordEncoder encoder, JWTUtils jwtUtils, @Lazy AuthenticationManager authenticationManager, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userConverter = userConverter;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User", username));

        return UserConverter.toUserDetails(user);
    }

    @Override
    public void updateUser(UpdateUserRequest request) {
        User user = userRepository.findById(request.userID())
                .orElseThrow(() -> new NotFoundException("User", request.userID()));

        if (userRepository.existsByUsernameIgnoreCase(request.username()) || userRepository.existsByEmail(request.email())) {
            throw new UsernameOrEmailAlreadyExistsException(request.username(), request.email());
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setName(request.name());
        user.setSurname(request.surname());

        Set<Role> roles = userConverter.fromRoleNames(request.roles());
        user.setRoles(roles);
        user.setEnabled(request.enabled());

        userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        if(user.getId() == 1) throw DefaultEntityDeletionException.forDefaultAdmin();

        userRepository.deleteById(user.getId());
    }

    @Override
    public void activate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        if(user.isEnabled()){
            throw new UserAlreadyActivatedException(user.getId());
        }

        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username()) || userRepository.existsByEmail(request.email())) {
            throw new UsernameOrEmailAlreadyExistsException(request.username(), request.email());
        }

        User user = new User(
                request.username(),
                encoder.encode(request.password()),
                request.email(),
                request.name(),
                request.surname()
        );

        user.setRoles(Set.of(roleRepository.findByType(Role.Types.ROLE_USER)));

        try {
            User savedUser = userRepository.save(user);
            mailService.sendRegisterMessage(savedUser.getId(), savedUser.isEnabled());
        } catch (MessagingException ex) {
            throw new NotificationFailedException("Error occurred while sending registration notification", ex);
        }
    }

    @Override
    public LoginResponse authenticate(LoginRequest request) {
        userRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new NotFoundException("User", request.username()));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return UserConverter.toLoginResponse(userDetails, token);
    }

    @Override
    public UserDetailsResponse getUserDetails(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User", username));

        return UserConverter.toUserDetailsResponse(user);
    }

    @Override
    public void updateProfile(String username, UpdateProfileDetailsRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User", username));

        user.setName(request.name());
        user.setSurname(request.surname());
        user.setPassword(encoder.encode(request.password()));

        userRepository.save(user);
    }

    @Override
    public void add(AddUserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username()) || userRepository.existsByEmail(request.email())) {
            throw new UsernameOrEmailAlreadyExistsException(request.username(), request.email());
        }

        User user = new User(
                request.username(),
                encoder.encode(request.password()),
                request.email(),
                request.name(),
                request.surname()
        );

        Set<Role> roles = userConverter.fromRoleNames(request.roles());
        user.setRoles(roles);
        user.setEnabled(true);

        userRepository.save(user);
    }

    @Override
    public List<UserDetailsResponse> loadAll() {
        return userRepository.findAll().stream()
                .map(user -> UserConverter.toUserDetailsResponse(user))
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public UserDetailsResponse loadById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        return UserConverter.toUserDetailsResponse(user);
    }
}
