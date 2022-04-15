package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
    private Integer ticketID;

    @Size(min = 5, max = 100) @NotBlank
    @Column(name = "ticket_title", nullable = false)
    private String ticketTitle;

    @Size(min = 5, max = 500) @NotBlank
    @Column(name = "ticket_description", nullable = false)
    private String ticketDescription;

    @OneToMany
    @JoinColumn(name = "ticket_ticket_id")
    private List<Image> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ticket_date", nullable = false)
    private LocalDate ticketDate;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Category> categories;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "priorityID", nullable = false)
    private Priority priority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "statusID", nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "versionID", nullable = false)
    private Version version;

    @OneToMany
    @JoinColumn(name = "ticket_ticket_id")
    private List<TicketReply> ticketReplies = new ArrayList<>();

}
