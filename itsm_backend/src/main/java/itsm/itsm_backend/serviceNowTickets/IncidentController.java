package itsm.itsm_backend.serviceNowTickets;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final ServiceNowService serviceNowService;

    @GetMapping
    public List<IncidentDTO> getIncidents() {
        return serviceNowService.getAllIncidents();
    }
}