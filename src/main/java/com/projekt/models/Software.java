package com.projekt.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "software")
@NoArgsConstructor
@Getter
@Setter
public class Software {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, max = 30)
    @NotBlank
    @Column(nullable = false)
    private String name;

    @Size(min = 10, max = 200)
    @NotBlank
    @Column(nullable = false)
    private String description;

    public Software(String name, String description){
        this.name = name;
        this.description = description;
    }
}
