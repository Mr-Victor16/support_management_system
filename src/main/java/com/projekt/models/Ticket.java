package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tickets")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "Ticket.searchByStatus", query = "select t From Ticket t where t.status.statusID = ?1")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(min = 5, max = 100)
    @NotBlank
    @Column(name = "ticket_title", nullable = false)
    private String title;

    @Size(min = 5, max = 500)
    @NotBlank
    @Column(name = "ticket_description", nullable = false)
    private String description;

    @OneToMany
    @JoinColumn(name = "ticket_ticket_id")
    private List<Image> images = new ArrayList<>();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ticket_date", nullable = false)
    private LocalDate date;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Category> categories;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "priorityID", nullable = false)
    private Priority priority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "statusID", nullable = false)
    private Status status;

    private String version;

    @ManyToOne
    @JoinColumn(name = "software_id")
    private Software software;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "ticket_id")
    private List<TicketReply> ticketReplies = new ArrayList<>();

}
