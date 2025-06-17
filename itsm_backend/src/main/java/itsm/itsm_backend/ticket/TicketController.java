package itsm.itsm_backend.ticket;

import io.swagger.v3.oas.annotations.tags.Tag;
import itsm.itsm_backend.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ticket")
@Tag(name = "Ticket")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/getAllTicket")
    public ResponseEntity<PageResponse<Ticket>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ticketService.findAll(page, size));
    }

    @GetMapping("/recipient")
    public ResponseEntity<PageResponse<Ticket>> getTicketsAsRecipient(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ticketService.getTicketsAsRecipient(page, size));
    }

    @GetMapping("/sender")
    public ResponseEntity<PageResponse<Ticket>> getTicketsAsSender(
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
    public ResponseEntity<Ticket> findById(@PathVariable("id") String id) {
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


}
