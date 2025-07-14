package itsm.itsm_backend.reportWithOllama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.ticket.jpa.MonthlyTrendReportRepository;
import itsm.itsm_backend.ticket.jpa.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrendAnalysisService {

    private final TicketRepository ticketRepository;
    private final MonthlyTrendReportRepository reportRepository;
    private final PdfReportService pdfReportService;


    private String buildPrompt(List<Ticket> tickets) {
        StringBuilder sb = new StringBuilder();
        sb.append("Voici une liste de tickets d'incidents IT du mois écoulé :\n\n");

        sb.append("STATISTIQUES GÉNÉRALES:\n");
        sb.append("- Nombre total de tickets : ").append(tickets.size()).append("\n");

        Map<String, Long> categoryStats = getCategoryStats(tickets);
        sb.append("- Répartition par catégorie :\n");
        categoryStats.forEach((category, count) ->
                sb.append("  * ").append(category).append(" : ").append(count).append(" tickets\n"));

        Map<String, Long> serviceStats = getServiceStats(tickets);
        sb.append("- Services les plus impactés :\n");
        serviceStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry ->
                        sb.append("  * ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(" tickets\n"));

        sb.append("\nDÉTAIL DES TICKETS:\n");
        for (Ticket t : tickets) {
            sb.append("- Titre : ").append(t.getTitle()).append("\n");
            sb.append("  Description : ").append(t.getDescription()).append("\n");
            sb.append("  Catégorie : ").append(t.getCategory()).append("\n");
            if (t.getSender() != null && t.getSender().getGroup() != null) {
                sb.append("  Service concerné : ").append(t.getSender().getGroup()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("""
        
        CONSIGNES D'ANALYSE:
        En tant qu'expert en analyse IT, peux-tu analyser ces données et me générer un rapport structuré en français contenant :
        
        1. ANALYSE DES TENDANCES :
        - Identification des incidents les plus fréquents
        - Analyse des patterns temporels si observables
        - Évolution par rapport aux périodes précédentes (si mentionné)
        
        2. IMPACT SUR LES SERVICES :
        - Services les plus touchés et impact business
        - Corrélations entre types d'incidents et services
        
        3. CAUSES RACINES IDENTIFIÉES :
        - Analyse des descriptions pour identifier les causes récurrentes
        - Problèmes systémiques détectés
        - Facteurs contributifs
        
        4. RECOMMANDATIONS STRATÉGIQUES :
        - Actions préventives prioritaires
        - Améliorations des processus suggérées
        - Formations ou ressources nécessaires
        - Métriques à surveiller
        
        5. PRIORISATION :
        - Classement des actions par impact/effort
        - Timeline suggérée pour les améliorations
        
        Le rapport doit être professionnel, actionnable et orienté solutions.
        """);

        return sb.toString();
    }

    private Map<String, Long> getServiceStats(List<Ticket> tickets) {
        return tickets.stream()
                .filter(t -> t.getSender() != null && t.getSender().getGroup() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getSender().getGroup().name(),
                        Collectors.counting()
                ));
    }

    private String callOllama(String prompt) throws IOException, InterruptedException {
        OllamaRequest ollamaRequest = new OllamaRequest("llama3", prompt);

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(ollamaRequest);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Réponse brute : " + response.body());

        return extractResponse(response.body());
    }


    private String extractResponse(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);

        JsonNode responseNode = root.get("response");
        if (responseNode == null) {
            throw new IllegalArgumentException("Champ 'response' introuvable dans la réponse : " + responseBody);
        }

        return responseNode.asText();
    }


    public Map<String, Long> getCategoryStats(List<Ticket> tickets) {
        return tickets.stream()
                .collect(Collectors.groupingBy(ticket -> ticket.getCategory().name(), Collectors.counting()));
    }

    public long getTotalTickets(List<Ticket> tickets) {
        return tickets.size();
    }



    public void generateMonthlyTrendReport() throws Exception {
        LocalDate start = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate end = LocalDate.now().withDayOfMonth(1);

        List<Ticket> recentTickets = ticketRepository.findByCreatedDateBetween(start.atStartOfDay(), end.atStartOfDay());
        if (recentTickets.isEmpty()) {
            System.out.println("Aucun ticket trouvé pour la période " + start + " à " + end);
            return;
        }

        System.out.println("Génération du rapport pour " + recentTickets.size() + " tickets...");

        String prompt = buildPrompt(recentTickets);
        String reportText = callOllama(prompt);

        MonthlyTrendReport report = new MonthlyTrendReport();
        report.setGeneratedDate(LocalDateTime.now());
        report.setPeriodLabel(start.getMonth().toString() + " " + start.getYear());
        report.setReportText(reportText);
        reportRepository.save(report);

        Map<String, Long> categoryStats = getCategoryStats(recentTickets);

        pdfReportService.generatePdf(recentTickets, reportText, categoryStats);

        System.out.println("Rapport mensuel généré avec succès !");
    }


    public void generateCustomPeriodReport(LocalDate startDate, LocalDate endDate) throws Exception {
        List<Ticket> tickets = ticketRepository.findByCreatedDateBetween(startDate.atStartOfDay(), endDate.atStartOfDay());

        if (tickets.isEmpty()) {
            System.out.println("Aucun ticket trouvé pour la période " + startDate + " à " + endDate);
            return;
        }

        String prompt = buildPrompt(tickets);
        String reportText = callOllama(prompt);
        Map<String, Long> categoryStats = getCategoryStats(tickets);

        pdfReportService.generatePdf(tickets, reportText, categoryStats);

        System.out.println("Rapport personnalisé généré pour la période " + startDate + " à " + endDate);
    }


    public String getQuickAnalysis(List<Ticket> tickets) throws IOException, InterruptedException {
        if (tickets.isEmpty()) {
            return "Aucun ticket à analyser.";
        }

        String prompt = buildPrompt(tickets);
        return callOllama(prompt);
    }
    public static class OllamaRequest {
        public String model;
        public String prompt;
        public boolean stream = false;
        public Map<String, Object> options;

        public OllamaRequest(String model, String prompt) {
            this.model = model;
            this.prompt = prompt;
            this.options = Map.of(
                    "temperature", 0.7,
                    "top_p", 0.9,
                    "max_tokens", 2000
            );
        }
    }

}