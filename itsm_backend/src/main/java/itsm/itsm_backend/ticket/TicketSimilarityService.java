package itsm.itsm_backend.ticket;

import itsm.itsm_backend.dashboard.UserLoadDTO;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class TicketSimilarityService {

  /*  private final RestTemplate restTemplate;
    private final TicketRepository ticketRepository;
    private final ExecutorService executorService;

    // Configuration values - can be externalized to application.properties
    @Value("${ollama.embed.url:http://localhost:11434/api/embed}")
    private String ollamaEmbedUrl;

    @Value("${ollama.embed.model:mxbai-embed-large}")
    private String embedModel;

    // Corrected similarity thresholds (lower values = higher similarity)
    private static final double HIGH_SIMILARITY_THRESHOLD = 0.3;    // Very similar
    private static final double MEDIUM_SIMILARITY_THRESHOLD = 0.6;  // Moderately similar
    private static final double LOW_SIMILARITY_THRESHOLD = 0.8;     // Loosely similar
    private static final double DEFAULT_SIMILARITY_THRESHOLD = MEDIUM_SIMILARITY_THRESHOLD;

    private static final int BATCH_SIZE = 100; // For bulk operations

    public TicketSimilarityService(TicketRepository ticketRepository) {
        this.restTemplate = new RestTemplate();
        this.ticketRepository = ticketRepository;
        this.executorService = Executors.newFixedThreadPool(5);
    }


    public List<TicketResponse> findSimilarTickets(String title, String description, int topK) {
        return findSimilarTickets(title, description, topK, DEFAULT_SIMILARITY_THRESHOLD);
    }


    public List<TicketResponse> findSimilarTickets(String title, String description, int topK, double threshold) {
        try {
            float[] embedding = generateEmbedding(title + ". " + description);
            String embeddingStr = arrayToVectorString(embedding);

            List<Ticket> similarTickets = ticketRepository
                    .findTopKSimilarWithThreshold(embeddingStr, threshold, topK);

            return similarTickets.stream()
                    .map(this::mapToTicketResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error finding similar tickets for title: {}, description: {}", title, description, e);
            return Collections.emptyList();
        }
    }


    public SimilarityGroupedResponse findSimilarTicketsByLevel(String title, String description) {
        try {
            float[] embedding = generateEmbedding(title + ". " + description);
            String embeddingStr = arrayToVectorString(embedding);

            // Fetch different similarity levels
            List<Ticket> highSimilarity = ticketRepository
                    .findTopKSimilarWithThreshold(embeddingStr, HIGH_SIMILARITY_THRESHOLD, 5);

            List<Ticket> mediumSimilarity = ticketRepository
                    .findTopKSimilarWithThreshold(embeddingStr, MEDIUM_SIMILARITY_THRESHOLD, 10);

            List<Ticket> lowSimilarity = ticketRepository
                    .findTopKSimilarWithThreshold(embeddingStr, LOW_SIMILARITY_THRESHOLD, 15);

            return SimilarityGroupedResponse.builder()
                    .highSimilarity(highSimilarity.stream().map(this::mapToTicketResponse).toList())
                    .mediumSimilarity(mediumSimilarity.stream().map(this::mapToTicketResponse).toList())
                    .lowSimilarity(lowSimilarity.stream().map(this::mapToTicketResponse).toList())
                    .build();

        } catch (Exception e) {
            log.error("Error finding similar tickets by level for title: {}, description: {}", title, description, e);
            return SimilarityGroupedResponse.builder()
                    .highSimilarity(Collections.emptyList())
                    .mediumSimilarity(Collections.emptyList())
                    .lowSimilarity(Collections.emptyList())
                    .build();
        }
    }


    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", embedModel,
                    "input", text.trim()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            Map<?, ?> response = restTemplate.postForObject(ollamaEmbedUrl, request, Map.class);

            if (response == null) {
                throw new IllegalStateException("No response from Ollama embedding service");
            }

            return extractEmbeddingFromResponse(response);

        } catch (RestClientException e) {
            log.error("Error calling Ollama embedding service for text: {}", text, e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    private float[] extractEmbeddingFromResponse(Map<?, ?> response) {
        // Try different possible response structures
        Object vecObj = Optional.ofNullable(response.get("embedding"))
                .orElseGet(() -> response.get("embeddings"));

        if (vecObj == null && response.get("data") instanceof Map<?, ?> dataMap) {
            vecObj = dataMap.get("embedding");
        }

        if (vecObj == null) {
            throw new IllegalStateException("No embedding found in response: " + response);
        }

        // Handle nested list structure
        List<?> list;
        if (vecObj instanceof List<?> outer && !outer.isEmpty() && outer.get(0) instanceof List<?>) {
            list = (List<?>) outer.get(0);
        } else if (vecObj instanceof List<?>) {
            list = (List<?>) vecObj;
        } else {
            throw new IllegalStateException("Unexpected embedding structure: " + response);
        }

        // Convert to float array
        float[] result = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object val = list.get(i);
            if (val instanceof Number num) {
                result[i] = num.floatValue();
            } else {
                throw new IllegalStateException("Non-numeric value in embedding at index " + i + ": " + val);
            }
        }

        return result;
    }


    @Transactional
    public void updateEmbeddingsForAllResolvedTickets() {
        log.info("Starting bulk embedding update for resolved tickets");

        long totalTickets = ticketRepository.countByStatusAndEmbeddingVectorIsNull(Status.RESOLVED);
        log.info("Found {} resolved tickets without embeddings", totalTickets);

        int page = 0;
        int processed = 0;

        while (true) {
            Pageable pageable = PageRequest.of(page, BATCH_SIZE);
            List<Ticket> tickets = ticketRepository.findByStatusAndEmbeddingVectorIsNull(Status.RESOLVED, pageable);

            if (tickets.isEmpty()) {
                break;
            }

            for (Ticket ticket : tickets) {
                try {
                    String text = buildTextForEmbedding(ticket);
                    float[] embedding = generateEmbedding(text);
                    ticket.setEmbedding(embedding);
                    processed++;

                    if (processed % 10 == 0) {
                        log.info("Processed {}/{} tickets", processed, totalTickets);
                    }
                } catch (Exception e) {
                    log.error("Failed to generate embedding for ticket {}: {}", ticket.getId(), e.getMessage());
                }
            }

            ticketRepository.saveAll(tickets);
            page++;
        }

        log.info("Completed embedding update. Processed {} tickets", processed);
    }


    public CompletableFuture<Void> updateEmbeddingsAsync() {
        return CompletableFuture.runAsync(this::updateEmbeddingsForAllResolvedTickets, executorService);
    }

    private String buildTextForEmbedding(Ticket ticket) {
        StringBuilder text = new StringBuilder();

        if (ticket.getTitle() != null && !ticket.getTitle().trim().isEmpty()) {
            text.append(ticket.getTitle().trim());
        }

        if (ticket.getDescription() != null && !ticket.getDescription().trim().isEmpty()) {
            if (text.length() > 0) {
                text.append(". ");
            }
            text.append(ticket.getDescription().trim());
        }

        // Add resolution notes if available for better similarity matching
        if (ticket.getResolution_notes() != null && !ticket.getResolution_notes().trim().isEmpty()) {
            if (text.length() > 0) {
                text.append(". Resolution: ");
            }
            text.append(ticket.getResolution_notes().trim());
        }

        return text.toString();
    }

    private String arrayToVectorString(float[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .priority(ticket.getPriority().name())
                .status(ticket.getStatus().name())
                .category(ticket.getCategory().name())
                .type(ticket.getType().name())
                .resolution_notes(ticket.getResolution_notes())
                .resolutionTime(ticket.getResolution_time())
                .sender(ticket.getSender() != null ? UserLoadDTO.builder()
                        .userId(ticket.getSender().getId())
                        .fullName(ticket.getSender().fullName())
                        .build() : null)
                .recipient(ticket.getRecipient() != null ? UserLoadDTO.builder()
                        .userId(ticket.getRecipient().getId())
                        .fullName(ticket.getRecipient().fullName())
                        .build() : null)
                .build();
    }

    // Response DTO for grouped similarity results
    @Data
    @Builder
    public static class SimilarityGroupedResponse {
        private List<TicketResponse> highSimilarity;
        private List<TicketResponse> mediumSimilarity;
        private List<TicketResponse> lowSimilarity;
    }*/
}