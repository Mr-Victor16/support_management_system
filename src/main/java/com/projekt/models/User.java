package com.projekt.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projekt.validators.FirstCharacterConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Table( name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@NoArgsConstructor
@Getter
@Setter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, max = 36)
    @NotBlank
    @Column(name = "username", nullable = false)
    private String username;

    @NotBlank
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false)
    private String email;

    @Size(min = 2, max = 30)
    @NotBlank
    @FirstCharacterConstraint
    @Column(name = "name", nullable = false)
    private String name;

    @Size(min = 2, max = 60)
    @NotBlank
    @FirstCharacterConstraint
    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @ManyToMany
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Ticket> tickets = new ArrayList<>();

    public User(String username, boolean enabled){
        this.username = username;
        this.enabled = enabled;
    }
}
