package itsm.itsm_backend.config;

import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.user.User;
import org.springframework.batch.item.ItemProcessor;

public class TicketProcessor implements ItemProcessor<Ticket, Ticket> {

    private final User connectedUser;

    public TicketProcessor(User connectedUser) {
        this.connectedUser = connectedUser;
    }

    @Override
    public Ticket process(Ticket ticket) {
        ticket.setSender(connectedUser);
        return ticket;
    }
}
