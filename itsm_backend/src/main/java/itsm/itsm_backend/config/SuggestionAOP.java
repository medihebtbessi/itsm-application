package itsm.itsm_backend.config;

import itsm.itsm_backend.ollamaSuggestion.OllamaService;
import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.ticket.jpa.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class SuggestionAOP {

    private final OllamaService ollamaService;
    private final TicketRepository ticketRepository;

    //@After("execution(* itsm.itsm_backend.ticket.TicketService.save(..))")
    //@Async
    public void afterSave(JoinPoint joinPoint) {

        Ticket ticket = ticketRepository.findAll().stream().findFirst().orElse(null);

        List<Double> embeddings = ollamaService.getEmbedding(ticket.getTitle()+". "+ticket.getDescription());
        if (!embeddings.isEmpty()) {
            ticket.setEmbeddings(embeddings);
            ticketRepository.save(ticket);
        }

        System.out.println("save() appelée. Lancement automatique d'une autre fonction.");
    }
}
