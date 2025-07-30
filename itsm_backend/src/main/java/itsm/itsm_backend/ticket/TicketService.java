package itsm.itsm_backend.ticket;

import itsm.itsm_backend.common.PageResponse;
import itsm.itsm_backend.dashboard.UserLoadDTO;
import itsm.itsm_backend.email.EmailService;
import itsm.itsm_backend.email.EmailTemplateName;
import itsm.itsm_backend.ollamaSuggestion.OllamaService;
import itsm.itsm_backend.reportWithOllama.TrendAnalysisService;
import itsm.itsm_backend.ticket.jpa.CommentRepository;
import itsm.itsm_backend.ticket.elastic.TicketElasticRepository;
import itsm.itsm_backend.ticket.jpa.TicketRepository;
import itsm.itsm_backend.user.Role;
import itsm.itsm_backend.user.User;
import itsm.itsm_backend.ticket.jpa.UserRepository;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static itsm.itsm_backend.ticket.Priority.LOW;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CommentRepository commentRepository;
    private final OllamaService llamaService;

    //private final TicketElasticRepository ticketElasticRepository;

    @Cacheable(value = "ticket-pages", key = "'recipient_' + #page + '_' + #size + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public PageResponse<TicketResponse> getTicketsAsRecipient(int page, int size) {
        User user = getUserInfo();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Ticket> tickets = ticketRepository.getTicketsAsRecipient(pageable, user.getId());
        return new PageResponse<>(
                tickets.stream().map(this::mapToTicketResponse).toList(),
                tickets.getNumber(),
                tickets.getSize(),
                tickets.getTotalElements(),
                tickets.getTotalPages(),
                tickets.isFirst(),
                tickets.isLast()
        );
    }

    @Cacheable(value = "ticket-pages", key = "'sender_' + #page + '_' + #size + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public PageResponse<TicketResponse> getTicketsAsSender(int page, int size) {
        User user = getUserInfo();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Ticket> tickets = ticketRepository.getTicketsAsSender(pageable, user.getId());

        return new PageResponse<>(
                tickets.stream().map(this::mapToTicketResponse).toList(),
                tickets.getNumber(),
                tickets.getSize(),
                tickets.getTotalElements(),
                tickets.getTotalPages(),
                tickets.isFirst(),
                tickets.isLast()
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "ticket-pages", allEntries = true),
            @CacheEvict(value = "ticket-search", allEntries = true)
    })
    public String save(Ticket ticket) {
        User user = getUserInfo();
        ticket.setSender(user);
        LocalDateTime dueDate;

        switch (ticket.getPriority()) {
            case CRITICAL -> dueDate = LocalDateTime.now().plusHours(2);
            case HIGH     -> dueDate = LocalDateTime.now().plusHours(8);
            case MEDIUM   -> dueDate = LocalDateTime.now().plusHours(24);
            case LOW     -> dueDate = LocalDateTime.now().plusDays(2);
            default         -> dueDate = LocalDateTime.now().plusDays(3); // fallback
        }

        ticket.setDueDate(dueDate);
        // ticket.setEmbeddings(llamaService.getEmbedding(ticket.getTitle()+". "+ticket.getDescription()));
        ticket = ticketRepository.save(ticket);
       /* TicketDocument document = TicketDocument.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .senderId(ticket.getSender().getId())
                //.recipientId(ticket.getRecipient() != null ? ticket.getRecipient().getId() : null)
                .priority(ticket.getPriority().name())
                .status(ticket.getStatus().name())
                .category(ticket.getCategory().name())
                .type(ticket.getType().name())
                .createdDate(LocalDateTime.now())
                .dueDate(dueDate)
                .build();

        ticketElasticRepository.save(document);*/

        //ticketElasticRepository.save(ticket);
        return ticket.getId();
    }

    @Cacheable(value = "ticket-single", key = "#id")
    public TicketResponse findById(String id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        return mapToTicketResponse(ticket);
    }

    @Caching(evict = {
            @CacheEvict(value = "ticket-single", key = "#ticketId"),
            @CacheEvict(value = "ticket-pages", allEntries = true),
            @CacheEvict(value = "ticket-search", allEntries = true)
    })
    public void delete(String ticketId) {
       // ticketElasticRepository.deleteById(ticketId);
        ticketRepository.deleteById(ticketId);
    }

    @Caching(evict = {
            @CacheEvict(value = "ticket-single", key = "#idTicket"),
            @CacheEvict(value = "ticket-pages", allEntries = true),
            @CacheEvict(value = "ticket-search", allEntries = true)
    })
    public String updateTicket(Ticket ticket, String idTicket) {
        Ticket ticketUpdated = ticketRepository.findById(idTicket).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        ticketUpdated.setTitle(ticket.getTitle());
        ticketUpdated.setDescription(ticket.getDescription());
        ticketUpdated.setCategory(ticket.getCategory());
        ticketUpdated.setResolution_notes(ticket.getResolution_notes());
        ticketUpdated.setStatus(ticket.getStatus());
        ticketUpdated.setPriority(ticket.getPriority());
        ticketUpdated.setType(ticket.getType());

       /* TicketDocument document = TicketDocument.builder()
                .id(idTicket)
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .senderId(ticketUpdated.getSender().getId())
                //.recipientId(ticketUpdated.getRecipient() != null ? ticket.getRecipient().getId() : null)
                .priority(ticket.getPriority().name())
                .status(ticket.getStatus().name())
                .category(ticket.getCategory().name())
                .type(ticket.getType().name())
                .createdDate(ticket.getCreatedDate())
                .build();

        ticketElasticRepository.save(document);*/
        return ticketRepository.save(ticketUpdated).getId();
    }

    @Cacheable(value = "ticket-pages", key = "'all_' + #page + '_' + #size + '_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public PageResponse<TicketResponse> findAll(int page, int size) {
        User user = getUserInfo();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        log.info("Data From Postgres");

        Page<Ticket> tickets;
        if (user.getRole().equals(Role.ENGINEER) || user.getRole().equals(Role.MANAGER)) {
            tickets = ticketRepository.findByCategorie(Category.valueOf(user.getGroup().name()), pageable);
        } else {
            tickets = ticketRepository.findAll(pageable);
        }

        List<TicketResponse> content = tickets.getContent()
                .stream()
                .map(this::mapToTicketResponse)
                .toList();

        return new PageResponse<>(
                content,
                tickets.getNumber(),
                tickets.getSize(),
                tickets.getTotalElements(),
                tickets.getTotalPages(),
                tickets.isFirst(),
                tickets.isLast()
        );
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ticket-single", key = "#ticketId"),
            @CacheEvict(value = "ticket-pages", allEntries = true),
            @CacheEvict(value = "ticket-search", allEntries = true)
    })
    public String ticketAsResolved(String ticketId, String resolutionNotes) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        ticket.setResolution_notes(resolutionNotes);
        ticket.setResolution_time(LocalDateTime.now());
        ticket.setStatus(Status.RESOLVED);
        return ticketRepository.save(ticket).getId();
    }

    @Caching(evict = {
            @CacheEvict(value = "ticket-single", key = "#ticketId"),
            @CacheEvict(value = "ticket-pages", allEntries = true)
    })
    public void assignTicketToUser(Integer userId, String ticketId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        ticket.setRecipient(user);
        ticket.setStatus(Status.IN_PROGRESS);
        ticketRepository.save(ticket);
    }

    public User getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            String email = null;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                email = principal.toString();
            }

            if (email != null) {
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        }

        return null;
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
                .createdDate(ticket.getCreatedDate())
                .sender(ticket.getSender() != null ? UserLoadDTO.builder()
                        .userId(ticket.getSender().getId())
                        .fullName(ticket.getSender().fullName())
                        .email(ticket.getSender().getEmail())
                        .build() : null)
                .recipient(ticket.getRecipient() != null ?
                        UserLoadDTO.builder()
                                .userId(ticket.getRecipient().getId())
                                .fullName(ticket.getRecipient().fullName())
                                .email(ticket.getRecipient().getEmail())
                                .build()
                        : null
                )
                .attachmentUrls(ticket.getAttachments().stream().map(Attachment::getUrl).toList())
                .comments(ticket.getComments().stream().map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .type(comment.getType().name() != null ? comment.getType().name() : null)
                        .creationDate(comment.getCreatedDate())
                        .user(UserLoadDTO.builder()
                                .userId(comment.getUser().getId())
                                .fullName(comment.getUser().fullName())
                                .email(comment.getUser().getEmail())
                                .build())
                        .build()).toList())
                .build();
    }

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void nettingTableOfTicketsNull() {
        ticketRepository.nettingTableWhereNull();
        log.info("Netting null Tickets already");
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void notifiyingTicketsNotAssignedAfterThreeDaysOfCreation() throws MessagingException {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Ticket> tickets = ticketRepository.findAllTicketsNotAssigned(threeDaysAgo);
        for (Ticket ticket : tickets) {
            emailService.sendEmailForTicketNotAssigned(ticket.getSender(), ticket.getSender().fullName(), EmailTemplateName.TICKET_NOT_ASSIGNED, ticket, "Ticket Not Assigned After 3 days");
        }
        log.info("Notified unassigned tickets");
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    @CacheEvict(value = "ticket-pages", allEntries = true)
    public void makeAllTicketResolvedHas5DaysAsClosed() throws MessagingException {
        List<Ticket> tickets = ticketRepository.findAllByStatusAsResolved(LocalDateTime.now().minusDays(5));
        for (Ticket ticket : tickets) {
            ticket.setStatus(Status.CLOSED);
            ticketRepository.save(ticket);
            emailService.sendEmailForTicketNotAssigned(ticket.getSender(), ticket.getSender().fullName(), EmailTemplateName.TICKET_NOT_ASSIGNED, ticket, "Your ticket has been changed Status to Closed after 5 days of resolution");
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ticket-single", key = "#ticketId"),
            @CacheEvict(value = "ticket-pages", allEntries = true)
    })
    public String addCommentToTicket(String ticketId, Comment comment) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        User user = getUserInfo();
        comment.setUser(user);
        comment.setTicket(ticket);
        comment = commentRepository.save(comment);
        /*List<Comment> comments = ticket.getComments();
        comments.add(comment);
        ticket.setComments(comments);*/
        return ticketId;
    }

    //@Scheduled(fixedRate = 300_000)
    @Scheduled(fixedDelay = 30000)
    @CacheEvict(value = "ticket-pages", allEntries = true)
    public void processEmails() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getInstance(props, null);
        Store store = session.getStore();
        store.connect("imap.gmail.com", "ihebtbessi37@gmail.com", "lcvn lgcc qlnw vdtc");

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        int messageCount = inbox.getMessageCount();
        int start = Math.max(1, messageCount - 9);
        Message[] messages = inbox.getMessages(start, messageCount);

        for (int i = messages.length - 1; i >= 0; i--) {
            Message message = messages[i];

            if (!message.isSet(Flags.Flag.SEEN)) {
                try {
                    String title = message.getSubject();
                    String description = getTextFromMessage(message);

                    Map<String, String> fields = llamaService.analyzeTicket(title, description);
                    if (fields == null || fields.get("priority") == null || fields.get("status") == null ||
                            fields.get("category") == null || fields.get("type") == null) {

                        log.warn("Analyse IA incomplète ou invalide pour l'email : {}", title);
                        continue;
                    }

                    Address[] fromAddresses = message.getFrom();
                    String fromEmail = fromAddresses.length > 0 ? ((InternetAddress) fromAddresses[0]).getAddress() : null;

                    User user = userRepository.findByEmail(fromEmail.toLowerCase())
                            .orElseThrow(() -> new EntityNotFoundException("User not found "+fromEmail));

                    Ticket t = new Ticket();
                    t.setTitle(title);
                    t.setDescription(description);
                    t.setPriority(Priority.valueOf(fields.get("priority").toUpperCase()));
                    //t.setStatus(Status.valueOf(fields.get("status").toUpperCase()));
                    t.setStatus(Status.NEW);
                    t.setCategory(Category.valueOf(fields.get("category").toUpperCase()));
                    t.setType(TypeProbleme.valueOf(fields.get("type").toUpperCase()));
                    t.setSender(user);
                    LocalDateTime dueDate;

                    switch (t.getPriority()) {
                        case CRITICAL -> dueDate = LocalDateTime.now().plusHours(2);
                        case HIGH     -> dueDate = LocalDateTime.now().plusHours(8);
                        case MEDIUM   -> dueDate = LocalDateTime.now().plusHours(24);
                        case LOW     -> dueDate = LocalDateTime.now().plusDays(2);
                        default         -> dueDate = LocalDateTime.now().plusDays(3); // fallback
                    }

                    t.setDueDate(dueDate);

                    ticketRepository.save(t);
                    message.setFlag(Flags.Flag.SEEN, true);

                } catch (Exception e) {
                    log.error("Erreur lors du traitement de l'email : {}", message.getSubject(), e);
                }
            }
        }

        inbox.close(false);
        store.close();
        log.info("Traitement des 10 derniers emails terminé.");
    }

    public String getTextFromMessage(Message message) throws Exception {
        Object content = message.getContent();

        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);

                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    continue;
                }

                if (part.isMimeType("text/plain")) {
                    return (String) part.getContent();
                } else if (part.isMimeType("text/html")) {

                    String html = (String) part.getContent();
                    return org.jsoup.Jsoup.parse(html).text();
                }
            }
        }

        return "";
    }



   /* @Cacheable(value = "ticket-search", key = "#keyword")
    public List<TicketDocument> searchByTitle(String keyword) {
        return ticketElasticRepository.findByTitleContaining(keyword);
    }*/

    private final TrendAnalysisService trendAnalysisService;

    //@Scheduled(cron = "0 0 1 1 * ?")
    //@Scheduled(fixedRate = 30000)
    public void scheduleMonthlyTrendReport() throws IOException, InterruptedException {
        log.info("Rapport en cours de réalisation");
       // trendAnalysisService.generateMonthlyTrendReport();
    }



}