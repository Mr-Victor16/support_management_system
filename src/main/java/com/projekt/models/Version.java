package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "versions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Version {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer versionID;

    @Min(1970) @Max(2050)
    @Column(name = "version_year", nullable = false)
    private int versionYear;

    @Min(1) @Max(12)
    @Column(name = "version_month", nullable = false)
    private int versionMonth;

    @PositiveOrZero
    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "softwareID", nullable = false)
    private Software software;

    public Version(int versionYear, int versionMonth, int versionNumber){
        this.versionYear = versionYear;
        this.versionMonth = versionMonth;
        this.versionNumber = versionNumber;
    }

    @Override
    public String toString(){
        return versionYear + "." + versionMonth + "." + versionNumber;
    }
}
