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

@Entity
@Table(name = "tickets")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 5, max = 100)
    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @Size(min = 5, max = 500)
    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "ticket_id")
    private List<Image> images = new ArrayList<>();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "priority_id", nullable = false)
    private Priority priority;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Size(min = 1, max = 10)
    @NotBlank
    @Column(name = "version", nullable = false)
    private String version;

    @ManyToOne
    @JoinColumn(name = "software_id", nullable = false)
    private Software software;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "ticket_id")
    private List<TicketReply> replies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
