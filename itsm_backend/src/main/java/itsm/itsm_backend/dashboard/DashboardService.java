package itsm.itsm_backend.dashboard;

import itsm.itsm_backend.ticket.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TicketRepository ticketRepository;

    public DashboardOverviewDTO getOverviewStats(){
        List<Ticket> tickets = ticketRepository.findAll();
        long totalTickets = tickets.size();
        long openTickets= tickets.stream().filter(ticket -> Status.NEW.equals(ticket.getStatus())).count();
        long inProgressTickets= tickets.stream().filter(ticket -> Status.IN_PROGRESS.equals(ticket.getStatus())).count();
        long resolvedTickets= tickets.stream().filter(ticket -> Status.RESOLVED.equals(ticket.getStatus())).count();
        double averageResolutionTimeInHours=tickets.stream()
                .filter(ticket -> Status.RESOLVED.equals(ticket.getStatus())
                        && ticket.getCreatedDate() != null
                        && ticket.getResolution_time() != null)
                .mapToLong(ticket -> Duration.between(ticket.getCreatedDate(), ticket.getResolution_time()).toHours())
                .average()
                .orElse(0.0);
        return DashboardOverviewDTO.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .averageResolutionTimeInHours(averageResolutionTimeInHours)
                .build();
    }

    public Map<String, Long> getTicketsByStatus(){
        List<Ticket> tickets = ticketRepository.findAll();
        long openTickets= tickets.stream().filter(ticket -> Status.NEW.equals(ticket.getStatus())).count();
        long inProgressTickets= tickets.stream().filter(ticket -> Status.IN_PROGRESS.equals(ticket.getStatus())).count();
        long onHoldTickets= tickets.stream().filter(ticket -> Status.ON_HOLD.equals(ticket.getStatus())).count();
        long closed=tickets.stream().filter(ticket -> Status.CLOSED.equals(ticket.getStatus())).count();
        Map<String, Long> map = new HashMap<>();
        map.put(Status.NEW.name(), openTickets);
        map.put(Status.IN_PROGRESS.name(), inProgressTickets);
        map.put(Status.RESOLVED.name(), onHoldTickets);
        map.put(Status.CLOSED.name(), closed);
        map.put(Status.ON_HOLD.name(), openTickets);
        return map;
    }

    public Map<String, Long> getTicketsByPriority(){
        List<Ticket> tickets = ticketRepository.findAll();
        long lowTickets= tickets.stream().filter(ticket -> Priority.LOW.equals(ticket.getPriority())).count();
        long mediumTickets= tickets.stream().filter(ticket -> Priority.MEDIUM.equals(ticket.getPriority())).count();
        long highTickets= tickets.stream().filter(ticket -> Priority.HIGH.equals(ticket.getPriority())).count();
        long criticalTickets=tickets.stream().filter(ticket -> Priority.CRITICAL.equals(ticket.getPriority())).count();
        Map<String, Long> map = new HashMap<>();
        map.put(Priority.LOW.name(), lowTickets);
        map.put(Priority.MEDIUM.name(), mediumTickets);
        map.put(Priority.HIGH.name(), highTickets);
        map.put(Priority.CRITICAL.name(), criticalTickets);
        return map;
    }

    public Map<String, Long> getTicketsByCategory(){
        List<Ticket> tickets = ticketRepository.findAll();
        long hardwareTickets=tickets.stream().filter(ticket -> Category.HARDWARE.equals(ticket.getCategory())).count();
        long softwareTickets=tickets.stream().filter(ticket -> Category.SOFTWARE.equals(ticket.getCategory())).count();
        long networkTickets=tickets.stream().filter(ticket -> Category.NETWORK.equals(ticket.getCategory())).count();
        long otherTickets=tickets.stream().filter(ticket -> Category.OTHER.equals(ticket.getCategory())).count();
        Map<String, Long> map = new HashMap<>();
        map.put(Category.HARDWARE.name(), hardwareTickets);
        map.put(Category.SOFTWARE.name(), softwareTickets);
        map.put(Category.NETWORK.name(), networkTickets);
        map.put(Category.OTHER.name(), otherTickets);
        return map;
    }

    public List<Ticket> getUrgentTickets(){
        return ticketRepository.findAll().stream().filter(ticket -> Priority.HIGH.equals(ticket.getPriority()) || Priority.CRITICAL.equals(ticket.getPriority())).toList();
    }

    public List<UserLoadDTO> getLoadByRecipient(){
        return ticketRepository.getLoadByRecipient();
    }
}
