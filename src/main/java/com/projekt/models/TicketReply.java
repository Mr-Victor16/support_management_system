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

@Entity
@Table(name = "replies")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer replyID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @Size(min = 5, max = 500) @NotBlank
    @Column(name = "reply_content", nullable = false)
    private String replyContent;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "reply_date", nullable = false)
    private LocalDate replyDate;

    public TicketReply(String replyContent){
        this.replyContent = replyContent;
    }
}
