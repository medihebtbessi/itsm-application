package itsm.itsm_backend.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Document(indexName = "tickets")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDocument {
    private String id;
    private String title;
    private String description;
    private Integer senderId;
    private Integer recipientId;
    private String priority;
    private String status;
    private String category;
    private String type;
    private LocalDateTime createdDate;
    private LocalDateTime dueDate;
}
