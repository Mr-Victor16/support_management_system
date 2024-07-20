package com.projekt.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {
    private Long id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String token;
    private List<String> roles;
    private String type = "Bearer";

    public LoginResponse(Long id, String username, String name, String surname, String email, String token, List<String> roles) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.token = token;
        this.roles = roles;
    }
}
