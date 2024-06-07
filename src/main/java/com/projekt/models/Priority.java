package com.projekt.models;

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
    private Integer priorityID;

    @NotBlank @Size(min = 4, max = 20)
    @Column(name = "priority_name", nullable = false)
    private String priorityName;

    @Min(1)
    @Column(name = "max_time", nullable = false)
    private Integer maxTime;
}
