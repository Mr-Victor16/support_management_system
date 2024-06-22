package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Base64;

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
    private Integer imageID;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Lob
    @Column(columnDefinition="BLOB", nullable = false)
    private byte[] fileContent;

    public String getB64Content(){
        return Base64.getEncoder().encodeToString(this.fileContent);
    }
}
