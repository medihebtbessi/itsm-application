package itsm.itsm_backend.ollamaSuggestion;

import itsm.itsm_backend.ticket.Ticket;

import java.time.LocalDateTime;

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

    // Getters et Setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
}
