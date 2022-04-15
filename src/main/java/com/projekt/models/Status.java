package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "statuses")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statusID;

    @Size(min = 2, max = 20) @NotBlank
    @Column(name = "status_name", nullable = false)
    private String statusName;

    @Column(name = "close_ticket", nullable = false)
    private boolean closeTicket;
}
