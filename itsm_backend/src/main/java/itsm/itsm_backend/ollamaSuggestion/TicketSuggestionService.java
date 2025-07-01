package itsm.itsm_backend.ollamaSuggestion;

import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketSuggestionService {


    private final TicketRepository ticketRepository;


    private final OllamaService ollamaService;

    public List<TicketSuggestion> findSimilarTickets(String title, String description, String excludeId, int limit) {
        String inputText = title + ". " + description;

        List<Double> inputEmbedding = ollamaService.getEmbedding(inputText);

        if (inputEmbedding.isEmpty()) {
            return new ArrayList<>();
        }

        List<Ticket> activeTickets = excludeId != null ?
                ticketRepository.findActiveTicketsExcluding(excludeId) :
                ticketRepository.findActiveTickets();

        List<TicketSuggestion> suggestions = new ArrayList<>();

        for (Ticket ticket : activeTickets) {
            String ticketText = ticket.getTitle() + ". " + ticket.getDescription();
            List<Double> ticketEmbedding = ollamaService.getEmbedding(ticketText);

            if (!ticketEmbedding.isEmpty()) {
                double similarity = ollamaService.calculateCosineSimilarity(inputEmbedding, ticketEmbedding);

                if (similarity > 0.3) {
                    suggestions.add(new TicketSuggestion(ticket, similarity));
                }
            }
        }

        return suggestions.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
