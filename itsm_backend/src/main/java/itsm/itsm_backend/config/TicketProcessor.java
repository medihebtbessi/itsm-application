package itsm.itsm_backend.config;

import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.user.User;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;

public class TicketProcessor implements ItemProcessor<Ticket, Ticket> {

    private final User connectedUser;

    public TicketProcessor(User connectedUser) {
        this.connectedUser = connectedUser;
    }

    @Override
    public Ticket process(Ticket ticket) {

        if (ticket.getPriority()!=null){
            LocalDateTime dueDate;
            switch (ticket.getPriority()) {
                case CRITICAL -> dueDate = LocalDateTime.now().plusHours(2);
                case HIGH     -> dueDate = LocalDateTime.now().plusHours(8);
                case MEDIUM   -> dueDate = LocalDateTime.now().plusHours(24);
                case LOW     -> dueDate = LocalDateTime.now().plusDays(2);
                default         -> dueDate = LocalDateTime.now().plusDays(3); // fallback
            }
            ticket.setDueDate(dueDate);
        }

        ticket.setSender(connectedUser);

        return ticket;
    }
}
