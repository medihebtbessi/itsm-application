package itsm.itsm_backend.dashboard;

import io.swagger.v3.oas.annotations.tags.Tag;
import itsm.itsm_backend.ticket.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;


    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDTO> getOverview() {
        return ResponseEntity.ok(dashboardService.getOverviewStats());
    }


    @GetMapping("/tickets-by-status")
    public ResponseEntity<Map<String, Long>> getTicketsByStatus() {
        return ResponseEntity.ok(dashboardService.getTicketsByStatus());
    }


    @GetMapping("/tickets-by-priority")
    public ResponseEntity<Map<String, Long>> getTicketsByPriority() {
        return ResponseEntity.ok(dashboardService.getTicketsByPriority());
    }


    @GetMapping("/tickets-by-category")
    public ResponseEntity<Map<String, Long>> getTicketsByCategory() {
        return ResponseEntity.ok(dashboardService.getTicketsByCategory());
    }


    @GetMapping("/urgent-tickets")
    public ResponseEntity<List<Ticket>> getUrgentTickets() {
        return ResponseEntity.ok(dashboardService.getUrgentTickets());
    }

    @GetMapping("/load-by-recipient")
    public ResponseEntity<List<UserLoadDTO>> getLoadByRecipient() {
        return ResponseEntity.ok(dashboardService.getLoadByRecipient());
    }
}
