package com.projekt.models;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "priorities")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Priority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 20)
    @Column(name = "name", nullable = false)
    private String name;

    @Min(1)
    @NotNull
    @Column(name = "max_time", nullable = false)
    private Integer maxTime;

    public Priority(String name, Integer maxTime) {
        this.name = name;
        this.maxTime = maxTime;
    }
}
