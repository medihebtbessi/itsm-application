package itsm.itsm_backend.serviceNowTickets;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceNowService {

    @Value("${servicenow.url}")
    private String serviceNowUrl;

    @Value("${servicenow.username}")
    private String username;

    @Value("${servicenow.password}")
    private String password;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<IncidentDTO> getAllIncidents() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<ServiceNowResponse> response = restTemplate.exchange(
                serviceNowUrl,
                HttpMethod.GET,
                request,
                ServiceNowResponse.class
        );

        return response.getBody().getResult();
    }
}

