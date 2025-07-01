package itsm.itsm_backend.serviceNowTickets;

import lombok.Data;

import java.util.List;

@Data
public class ServiceNowResponse {
    private List<IncidentDTO> result;
}
