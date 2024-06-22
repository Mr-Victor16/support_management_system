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

@Entity
@Table(name = "replies")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @Size(min = 5, max = 500)
    @NotBlank
    @Column(name = "reply_content", nullable = false)
    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "reply_date", nullable = false)
    private LocalDate date;

}
