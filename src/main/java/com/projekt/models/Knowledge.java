package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(name = "knowledge_bases")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Knowledge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, max = 50)
    @NotBlank
    @Column(nullable = false)
    private String title;

    @Size(min = 20, max = 360)
    @NotBlank
    @Column(nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDate createdDate;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Software software;

    public Knowledge(String title, String content, Software software) {
        this.title = title;
        this.content = content;
        this.software = software;
    }

    public Knowledge(Long id, String title, String content, Software software) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.software = software;
    }
}
