package itsm.itsm_backend.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CalendarSlaEventDto {
    private String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;

    // Constructeurs, getters, setters
}
