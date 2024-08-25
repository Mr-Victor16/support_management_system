package com.projekt.payload.request.add;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddTicketReplyRequest {
    @NotNull
    private Long ticketID;

    @NotNull
    private Long userID;

    @Size(min = 5, max = 500)
    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
