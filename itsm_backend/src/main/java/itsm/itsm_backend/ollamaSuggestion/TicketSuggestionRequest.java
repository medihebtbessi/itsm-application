package itsm.itsm_backend.ollamaSuggestion;

public class TicketSuggestionRequest {
    private String title;
    private String description;
    private String excludeId;
    private Integer limit;

    // Constructeurs
    public TicketSuggestionRequest() {}

    // Getters et Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExcludeId() { return excludeId; }
    public void setExcludeId(String excludeId) { this.excludeId = excludeId; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
}
