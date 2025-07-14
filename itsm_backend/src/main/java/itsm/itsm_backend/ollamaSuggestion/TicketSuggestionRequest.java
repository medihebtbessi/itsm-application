package itsm.itsm_backend.ollamaSuggestion;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TicketSuggestionRequest {
    private String title;
    private String description;
    private String excludeId;
    private Integer limit;

    public TicketSuggestionRequest() {}

}
