package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "statuses")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(min = 2, max = 20)
    @NotBlank
    @Column(name = "status_name", nullable = false)
    private String name;

    @Column(name = "close_ticket", nullable = false)
    private boolean closeTicket;
}
