package itsm.itsm_backend.ticket;

import io.swagger.v3.oas.annotations.tags.Tag;
import itsm.itsm_backend.common.PageResponse;
import itsm.itsm_backend.ollamaSuggestion.TicketSuggestion;
import itsm.itsm_backend.ollamaSuggestion.TicketSuggestionRequest;
import itsm.itsm_backend.ollamaSuggestion.TicketSuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ticket")
@Tag(name = "Ticket")
public class TicketController {

    private final TicketService ticketService;
    private final TicketSimilarityService ticketSimilarityService;

    @GetMapping("/getAllTicket")
    public ResponseEntity<PageResponse<TicketResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ticketService.findAll(page, size));
    }

    @GetMapping("/recipient")
    public ResponseEntity<PageResponse<TicketResponse>> getTicketsAsRecipient(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ticketService.getTicketsAsRecipient(page, size));
    }

    @GetMapping("/sender")
    public ResponseEntity<PageResponse<TicketResponse>> getTicketsAsSender(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ticketService.getTicketsAsSender(page, size));
    }



    @PostMapping("/createTicket")
    public ResponseEntity<String> create(@Valid @RequestBody Ticket ticket) {
        String id = ticketService.save(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable("id") String ticketId,
            @Valid @RequestBody Ticket ticket) {

        String id = ticketService.updateTicket(ticket, ticketId);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        ticketService.delete(id);
    }

    @PutMapping("/ticketAsResolved")
    public ResponseEntity<String> updateTicketAsResolved(@RequestParam String ticketId,@RequestParam String resolutionNotes){
        return ResponseEntity.ok(ticketService.ticketAsResolved(ticketId,resolutionNotes));
    }

    @PutMapping("/assignToUser")
    public  void assignTicketToUser(Integer userId,String ticketId){
        ticketService.assignTicketToUser(userId,ticketId);
    }

   /* @PostMapping("/similar")
    public List<TicketResponse> getSimilarTickets(@RequestBody Ticket incomingTicket,
                                          @RequestParam(defaultValue = "10") int topK) {
        return ticketSimilarityService.findSimilarTickets(incomingTicket.getTitle(),
                incomingTicket.getDescription(),
                topK);
    }
    @PostMapping("/update-embeddings")
    public ResponseEntity<String> updateEmbeddings() {
        ticketSimilarityService.updateEmbeddingsForAllResolvedTickets();
        return ResponseEntity.ok("Embeddings updated.");
    }*/


    private final TicketSuggestionService suggestionService;

    @PostMapping("/suggestions")
    public ResponseEntity<List<TicketSuggestion>> getSuggestions(@RequestBody TicketSuggestionRequest request) {
        try {
            List<TicketSuggestion> suggestions = suggestionService.findSimilarTickets(
                    request.getTitle(),
                    request.getDescription(),
                    request.getExcludeId(),
                    request.getLimit() != null ? request.getLimit() : 10
            );

            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }



}
