package itsm.itsm_backend.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardOverviewDTO {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private double averageResolutionTimeInHours;
}