package itsm.itsm_backend.ticket;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/tickets/similarity")
@RequiredArgsConstructor
@Validated
@Tag(name = "Ticket Similarity", description = "Operations for finding similar tickets using AI embeddings")
public class TicketSimilarityController {

   /* private final TicketSimilarityService similarityService;


    @PostMapping("/search")
    @Operation(summary = "Find similar tickets",
            description = "Find tickets similar to the provided title and description using AI embeddings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Similar tickets found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SimilaritySearchResponse> findSimilarTickets(
            @Valid @RequestBody SimilaritySearchRequest request) {

        log.info("Searching for similar tickets with title: {} and topK: {}",
                request.getTitle(), request.getTopK());

        try {
            List<TicketResponse> similarTickets = similarityService.findSimilarTickets(
                    request.getTitle(),
                    request.getDescription(),
                    request.getTopK()
            );

            SimilaritySearchResponse response = SimilaritySearchResponse.builder()
                    .query(request)
                    .results(similarTickets)
                    .totalFound(similarTickets.size())
                    .threshold(0.6)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error finding similar tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimilaritySearchResponse.builder()
                            .query(request)
                            .results(List.of())
                            .totalFound(0)
                            .error("Failed to search for similar tickets: " + e.getMessage())
                            .build());
        }
    }


    @PostMapping("/search/advanced")
    @Operation(summary = "Advanced similarity search",
            description = "Find similar tickets with custom similarity threshold")
    public ResponseEntity<SimilaritySearchResponse> findSimilarTicketsAdvanced(
            @Valid @RequestBody AdvancedSimilaritySearchRequest request) {

        log.info("Advanced similarity search with threshold: {}", request.getThreshold());

        try {
            List<TicketResponse> similarTickets = similarityService.findSimilarTickets(
                    request.getTitle(),
                    request.getDescription(),
                    request.getTopK(),
                    request.getThreshold()
            );

            SimilaritySearchResponse response = SimilaritySearchResponse.builder()
                    .query(SimilaritySearchRequest.builder()
                            .title(request.getTitle())
                            .description(request.getDescription())
                            .topK(request.getTopK())
                            .build())
                    .results(similarTickets)
                    .totalFound(similarTickets.size())
                    .threshold(request.getThreshold())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in advanced similarity search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimilaritySearchResponse.builder()
                            .results(List.of())
                            .totalFound(0)
                            .error("Failed to perform advanced search: " + e.getMessage())
                            .build());
        }
    }


    @PostMapping("/search/grouped")
    @Operation(summary = "Grouped similarity search",
            description = "Find similar tickets grouped by similarity levels (high, medium, low)")
    //@PreAuthorize("hasRole('USER')")
    public ResponseEntity<TicketSimilarityService.SimilarityGroupedResponse> findSimilarTicketsByLevel(
            @Valid @RequestBody SimilaritySearchRequest request) {

        log.info("Grouped similarity search for title: {}", request.getTitle());

        try {
            TicketSimilarityService.SimilarityGroupedResponse response =
                    similarityService.findSimilarTicketsByLevel(request.getTitle(), request.getDescription());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in grouped similarity search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TicketSimilarityService.SimilarityGroupedResponse.builder()
                            .highSimilarity(List.of())
                            .mediumSimilarity(List.of())
                            .lowSimilarity(List.of())
                            .build());
        }
    }


    @GetMapping("/{ticketId}/similar")
    @Operation(summary = "Find similar tickets for existing ticket",
            description = "Find tickets similar to an existing ticket by ID")
    //@PreAuthorize("hasRole('USER')")
    public ResponseEntity<SimilaritySearchResponse> findSimilarToExistingTicket(
            @Parameter(description = "Ticket ID") @PathVariable String ticketId,
            @Parameter(description = "Maximum number of similar tickets to return")
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int topK,
            @Parameter(description = "Similarity threshold (0.0 to 1.0, lower means more similar)")
            @RequestParam(defaultValue = "0.6") @Min(0) @Max(1) double threshold) {

        log.info("Finding similar tickets for ticket ID: {}", ticketId);

        try {
            // This would require a method in the service to handle existing tickets
            // For now, we'll return a placeholder response
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(SimilaritySearchResponse.builder()
                            .results(List.of())
                            .totalFound(0)
                            .error("Feature not yet implemented")
                            .build());

        } catch (Exception e) {
            log.error("Error finding similar tickets for ticket ID: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimilaritySearchResponse.builder()
                            .results(List.of())
                            .totalFound(0)
                            .error("Failed to find similar tickets: " + e.getMessage())
                            .build());
        }
    }


    @PostMapping("/embedding")
    @Operation(summary = "Generate text embedding",
            description = "Generate AI embedding vector for given text")
   // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmbeddingResponse> generateEmbedding(
            @Valid @RequestBody EmbeddingRequest request) {

        log.info("Generating embedding for text of length: {}", request.getText().length());

        try {
            float[] embedding = similarityService.generateEmbedding(request.getText());

            EmbeddingResponse response = EmbeddingResponse.builder()
                    .text(request.getText())
                    .embedding(embedding)
                    .dimension(embedding.length)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating embedding", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmbeddingResponse.builder()
                            .text(request.getText())
                            .error("Failed to generate embedding: " + e.getMessage())
                            .build());
        }
    }


    @PostMapping("/embeddings/update")
    @Operation(summary = "Update embeddings for resolved tickets",
            description = "Trigger bulk update of embeddings for all resolved tickets without embeddings")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUpdateResponse> updateEmbeddings(
            @Parameter(description = "Run asynchronously")
            @RequestParam(defaultValue = "true") boolean async) {

        log.info("Triggering bulk embedding update, async: {}", async);

        try {
            if (async) {
                CompletableFuture<Void> future = similarityService.updateEmbeddingsAsync();

                return ResponseEntity.accepted()
                        .body(BulkUpdateResponse.builder()
                                .message("Bulk embedding update started asynchronously")
                                .async(true)
                                .status("STARTED")
                                .build());
            } else {
                similarityService.updateEmbeddingsForAllResolvedTickets();

                return ResponseEntity.ok(BulkUpdateResponse.builder()
                        .message("Bulk embedding update completed successfully")
                        .async(false)
                        .status("COMPLETED")
                        .build());
            }

        } catch (Exception e) {
            log.error("Error during bulk embedding update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BulkUpdateResponse.builder()
                            .message("Bulk embedding update failed: " + e.getMessage())
                            .status("FAILED")
                            .error(e.getMessage())
                            .build());
        }
    }


    @GetMapping("/stats")
    @Operation(summary = "Get similarity statistics",
            description = "Get statistics about ticket similarity processing")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SimilarityStatsResponse> getSimilarityStats() {
        log.info("Getting similarity statistics");

        try {
            // This would require additional methods in the service
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(SimilarityStatsResponse.builder()
                            .message("Statistics feature not yet implemented")
                            .build());

        } catch (Exception e) {
            log.error("Error getting similarity statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimilarityStatsResponse.builder()
                            .error("Failed to get statistics: " + e.getMessage())
                            .build());
        }
    }

    // Exception handler for validation errors
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            jakarta.validation.ConstraintViolationException e) {

        log.warn("Validation error: {}", e.getMessage());

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .error("Validation failed")
                        .message(e.getMessage())
                        .build());
    }

    // DTO Classes
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SimilaritySearchRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Description is required")
        private String description;

        @Min(value = 1, message = "topK must be at least 1")
        @Max(value = 50, message = "topK cannot exceed 50")
        private int topK = 5;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AdvancedSimilaritySearchRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Description is required")
        private String description;

        @Min(value = 1, message = "topK must be at least 1")
        @Max(value = 50, message = "topK cannot exceed 50")
        private int topK = 5;

        @Min(value = 0, message = "Threshold must be between 0 and 1")
        @Max(value = 1, message = "Threshold must be between 0 and 1")
        private double threshold = 0.6;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SimilaritySearchResponse {
        private SimilaritySearchRequest query;
        private List<TicketResponse> results;
        private int totalFound;
        private double threshold;
        private String error;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmbeddingRequest {
        @NotBlank(message = "Text is required")
        private String text;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmbeddingResponse {
        private String text;
        private float[] embedding;
        private int dimension;
        private String error;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BulkUpdateResponse {
        private String message;
        private boolean async;
        private String status;
        private String error;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SimilarityStatsResponse {
        private long totalTickets;
        private long ticketsWithEmbeddings;
        private long ticketsWithoutEmbeddings;
        private String lastUpdateTime;
        private String message;
        private String error;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String message;
        private String timestamp = java.time.LocalDateTime.now().toString();
    }*/
}