package com.projekt.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserDetails {
    private Long id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private List<String> roles;
}
