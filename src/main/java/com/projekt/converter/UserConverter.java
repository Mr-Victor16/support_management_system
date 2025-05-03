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

import java.util.Set;

@Component
public class UserConverter {
    private final RoleRepository roleRepository;

    public UserConverter(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public static UserDetailsResponse toUserDetailsResponse(User user){
        return new UserDetailsResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getRole().getType().name()
        );
    }

    public static UserDetails toUserDetails(User user) {
        Set<GrantedAuthority> grantedAuthorities = Set.of(new SimpleGrantedAuthority(user.getRole().getType().toString()));

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
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return new LoginResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getName(),
                userDetails.getSurname(),
                userDetails.getEmail(),
                token,
                role
        );
    }

    public Role fromRoleName(String roleName) {
        return roleRepository.findRoleByType(Role.Types.valueOf(roleName))
                .orElseThrow(() -> new NotFoundException("Role", roleName));
    }
}
