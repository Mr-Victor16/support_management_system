package com.projekt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Search {
    private String phrase;
    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date1;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date2;

    private Software software;

    private Version version;

    private Status status;
    private Priority priority;
    private Set<Category> categories;
    private Role role;
    private int number1;
    private int number2;
    private int type=1;
}
