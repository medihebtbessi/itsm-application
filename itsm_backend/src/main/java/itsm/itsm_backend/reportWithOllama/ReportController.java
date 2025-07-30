package itsm.itsm_backend.reportWithOllama;

import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.ticket.jpa.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor

public class ReportController {

    private final TrendAnalysisService trendAnalysisService;

    private final TicketRepository ticketRepository;

    @PostMapping("/monthly")
    public ResponseEntity<Map<String, Object>> generateMonthlyReport() {
        Map<String, Object> response = new HashMap<>();
        try {
            trendAnalysisService.generateMonthlyTrendReport();
            response.put("success", true);
            response.put("message", "Rapport mensuel généré avec succès");
            response.put("timestamp", LocalDate.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la génération du rapport : " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }


    @PostMapping("/custom")
    public ResponseEntity<Map<String, Object>> generateCustomReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDate endDate) {

        Map<String, Object> response = new HashMap<>();
        try {
            if (startDate.isAfter(endDate)) {
                response.put("success", false);
                response.put("message", "La date de début doit être antérieure à la date de fin");
                return ResponseEntity.badRequest().body(response);
            }

            trendAnalysisService.generateCustomPeriodReport(startDate, endDate);
            response.put("success", true);
            response.put("message", "Rapport personnalisé généré avec succès");
            response.put("period", startDate + " à " + endDate);
            response.put("timestamp", LocalDate.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la génération du rapport : " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }


    @GetMapping("/quick-analysis")
    public ResponseEntity<Map<String, Object>> getQuickAnalysis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDateTime endDate) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Si pas de dates spécifiées, prendre le mois dernier
            if (startDate == null || endDate == null) {
                startDate = LocalDateTime.now().minusMonths(1).withDayOfMonth(1);
                endDate = LocalDateTime.now().withDayOfMonth(1);
            }

            // Ici vous devrez adapter selon votre repository
             List<Ticket> tickets = ticketRepository.findByCreatedDateBetween(startDate, endDate);
             String analysis = trendAnalysisService.getQuickAnalysis(tickets);

            response.put("success", true);
            response.put("message", "Analyse rapide générée");
            response.put("period", startDate + " à " + endDate);
             response.put("analysis", analysis);
            response.put("timestamp", LocalDate.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'analyse : " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }


   /* @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkStatus() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Test simple de connectivité à Ollama
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:11434/api/tags"))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> httpResponse = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() == 200) {
                response.put("ollama_status", "✅ Connecté");
                response.put("ollama_available", true);
            } else {
                response.put("ollama_status", "❌ Erreur de connexion");
                response.put("ollama_available", false);
            }

            response.put("service_status", "✅ Service actif");
            response.put("timestamp", LocalDate.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("ollama_status", "❌ Non disponible");
            response.put("ollama_available", false);
            response.put("service_status", "⚠️ Service actif, Ollama indisponible");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDate.now().toString());
            return ResponseEntity.ok(response);
        }
    }*/
}