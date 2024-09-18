package com.projekt.converter;

import com.projekt.exceptions.NotFoundException;
import com.projekt.models.Role;
import com.projekt.models.User;
import com.projekt.payload.response.LoginResponse;
import com.projekt.payload.response.UserDetailsResponse;
import com.projekt.repositories.RoleRepository;
import com.projekt.security.services.UserDetailsImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserConverter {
    private final RoleRepository roleRepository;

    public UserConverter(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public static UserDetailsResponse toUserDetailsResponse(User user){
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

    public static UserDetails toUserDetails(User user) {
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

    public static LoginResponse toLoginResponse(UserDetailsImpl userDetails, String token) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

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

    public Set<Role> fromRoleNames(List<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findRoleByType(Role.Types.valueOf(roleName))
                        .orElseThrow(() -> new NotFoundException("Role", roleName)))
                .collect(Collectors.toSet());
    }
}
