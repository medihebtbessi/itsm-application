package itsm.itsm_backend.ticket;

import itsm.itsm_backend.dashboard.UserLoadDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private String id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String category;
    private String type;
    private String resolution_notes;
    private LocalDateTime resolutionTime;
    private LocalDateTime createdDate;

    private UserLoadDTO sender;
    private UserLoadDTO recipient;
    private List<String> attachmentUrls;
}

