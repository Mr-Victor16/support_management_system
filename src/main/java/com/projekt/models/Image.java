package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "images")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "content", columnDefinition="MEDIUMBLOB", nullable = false)
    private byte[] content;

    public Image(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }
}
