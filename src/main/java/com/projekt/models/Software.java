package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "software")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Software {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long softwareID;

    @Size(min = 2, max = 30) @NotBlank
    @Column(name = "software_name", nullable = false)
    private String softwareName;

    @Size(min = 10, max = 200) @NotBlank
    @Column(name = "software_description", nullable = false)
    private String softwareDescription;

    public Software(String softwareName, String softwareDescription){
        this.softwareName = softwareName;
        this.softwareDescription = softwareDescription;
    }
}
