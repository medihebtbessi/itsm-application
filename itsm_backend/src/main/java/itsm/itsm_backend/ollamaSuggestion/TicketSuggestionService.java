package itsm.itsm_backend.ollamaSuggestion;

import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.ticket.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketSuggestionService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private OllamaService ollamaService;

    public List<TicketSuggestion> findSimilarTickets(String title, String description, Long excludeId, int limit) {
        // Combiner titre et description pour l'analyse
        String inputText = title + ". " + description;

        // Obtenir l'embedding du ticket d'entrée
        List<Double> inputEmbedding = ollamaService.getEmbedding(inputText);

        if (inputEmbedding.isEmpty()) {
            return new ArrayList<>();
        }

        // Récupérer tous les tickets actifs (sauf celui en cours si spécifié)
        List<Ticket> activeTickets = excludeId != null ?
                ticketRepository.findActiveTicketsExcluding(excludeId) :
                ticketRepository.findActiveTickets();

        List<TicketSuggestion> suggestions = new ArrayList<>();

        // Calculer la similarité pour chaque ticket
        for (Ticket ticket : activeTickets) {
            String ticketText = ticket.getTitle() + ". " + ticket.getDescription();
            List<Double> ticketEmbedding = ollamaService.getEmbedding(ticketText);

            if (!ticketEmbedding.isEmpty()) {
                double similarity = ollamaService.calculateCosineSimilarity(inputEmbedding, ticketEmbedding);

                if (similarity > 0.3) { // Seuil de similarité minimum
                    suggestions.add(new TicketSuggestion(ticket, similarity));
                }
            }
        }

        // Trier par score de similarité décroissant et limiter les résultats
        return suggestions.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
