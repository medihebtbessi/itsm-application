package itsm.itsm_backend.ollamaSuggestion;

import itsm.itsm_backend.ticket.Ticket;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TicketSuggestion {

    private String ticketId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private LocalDateTime createdDate;
    private double similarityScore;

    public TicketSuggestion() {}

    public TicketSuggestion(Ticket ticket, double similarityScore) {
        this.ticketId = ticket.getId();
        this.title = ticket.getTitle();
        this.description = ticket.getDescription();
        this.status = ticket.getStatus().name();
        this.priority = ticket.getPriority().name();
        this.category = ticket.getCategory().name();
        this.createdDate = ticket.getCreatedDate();
        this.similarityScore = similarityScore;
    }

}
