package com.projekt.models;

import com.projekt.Validators.FirstCharacterConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(min = 2, max = 36) @NotBlank
    private String username;

    @NotEmpty @NotBlank
    private String password;

    @Transient
    private String passwordConfirm;

    @Email @NotEmpty
    private String email;

    @NotEmpty @Size(min = 2, max = 30) @NotBlank
    @FirstCharacterConstraint
    private String name;

    @NotEmpty @Size(min = 2, max = 60) @NotBlank
    @FirstCharacterConstraint
    private String surname;

    private boolean enabled = false;

    @ManyToMany
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public User(String username){
        this(username, false);
    }

    public User(String username, boolean enabled){
        this.username = username;
        this.enabled = enabled;
    }

    public User(String username, String password, String passwordConfirm){
        this.username = username;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }
}
