package itsm.itsm_backend.serviceNowTickets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentDTO {
    private String number;
    private String short_description;
    private String state;
}