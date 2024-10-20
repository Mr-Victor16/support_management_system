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
    private Long id;

    @Size(min = 2, max = 20)
    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(name = "close_ticket", nullable = false)
    private boolean closeTicket;

    @Column(name = "is_default", nullable = false)
    private boolean defaultStatus = false;

    public Status(Long id, String name, boolean closeTicket) {
        this.id = id;
        this.name = name;
        this.closeTicket = closeTicket;
    }

    public Status(String name, boolean closeTicket, boolean defaultStatus) {
        this.name = name;
        this.closeTicket = closeTicket;
        this.defaultStatus = defaultStatus;
    }

    public Status(String name, boolean closeTicket) {
        this.name = name;
        this.closeTicket = closeTicket;
    }
}
