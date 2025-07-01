package itsm.itsm_backend.ticket;

import io.swagger.v3.oas.annotations.tags.Tag;
import itsm.itsm_backend.common.PageResponse;
import itsm.itsm_backend.ollamaSuggestion.TicketSuggestion;
import itsm.itsm_backend.ollamaSuggestion.TicketSuggestionRequest;
import itsm.itsm_backend.ollamaSuggestion.TicketSuggestionService;
import itsm.itsm_backend.user.User;
import itsm.itsm_backend.user.UserRepository;
import itsm.itsm_backend.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ticket")
@Tag(name = "Ticket")
public class TicketController {

    private final TicketService ticketService;
    private final TicketSimilarityService ticketSimilarityService;
    private final JobLauncher jobLauncher;
    private final Job job;
    private  final UserRepository userService;
    private final TicketSuggestionService suggestionService;


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


    @PostMapping("/suggestions")
    public ResponseEntity<List<TicketSuggestion>> getSuggestions(@RequestBody TicketSuggestionRequest request) {
        try {
            List<TicketSuggestion> suggestions = suggestionService.findSimilarTickets(
                    request.getTitle(),
                    request.getDescription(),
                    request.getExcludeId(),
                    request.getLimit() != null ? request.getLimit() : 4
            );

            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }



    @PostMapping("/uploadCsv")
    public void importCsvToDBJob(@RequestParam("file")MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        Integer userId = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        Path tempDir = Files.createTempDirectory("uploads");
        Path tempFile = tempDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("csvPath", tempFile.toAbsolutePath().toString())
                .addLong("userId", userId.longValue())
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PutMapping("/comment/{id}")
    public ResponseEntity<?> addComment(@PathVariable("id") String ticketId,@RequestBody Comment comment) {
        return ResponseEntity.ok(ticketService.addCommentToTicket(ticketId, comment));
    }



}
