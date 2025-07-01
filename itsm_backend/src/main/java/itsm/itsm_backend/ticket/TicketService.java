package itsm.itsm_backend.ticket;

import itsm.itsm_backend.common.PageResponse;
import itsm.itsm_backend.dashboard.UserLoadDTO;
import itsm.itsm_backend.email.EmailService;
import itsm.itsm_backend.email.EmailTemplateName;
import itsm.itsm_backend.ollamaSuggestion.OllamaService;
import itsm.itsm_backend.user.Role;
import itsm.itsm_backend.user.User;
import itsm.itsm_backend.user.UserRepository;
import jakarta.mail.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CommentRepository commentRepository;
    private final OllamaService llamaService;

    public PageResponse<TicketResponse> getTicketsAsRecipient(int page, int size) {
       User user = getUserInfo();
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdDate").descending());
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
    public PageResponse<TicketResponse> getTicketsAsSender(int page, int size) {
        User user = getUserInfo();
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdDate").descending());
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
    public String save(Ticket ticket) {
        User user = getUserInfo();
        ticket.setSender(user);
        //ticket.setRecipient(user);
        return ticketRepository.save(ticket).getId();
    }
    public TicketResponse findById(String id) {
       Ticket ticket= ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        return mapToTicketResponse(ticket);
    }
    public void delete(String ticketId) {
        ticketRepository.deleteById(ticketId);
    }
    public String updateTicket(Ticket ticket,String idTicket) {
        Ticket ticketUpdated = ticketRepository.findById(idTicket).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        ticketUpdated.setTitle(ticket.getTitle());
        ticketUpdated.setDescription(ticket.getDescription());
        ticketUpdated.setCategory(ticket.getCategory());
        ticketUpdated.setResolution_notes(ticket.getResolution_notes());
        ticketUpdated.setStatus(ticket.getStatus());
        ticketUpdated.setPriority(ticket.getPriority());
        ticketUpdated.setType(ticket.getType());
        return ticketRepository.save(ticketUpdated).getId();
    }
    public PageResponse<TicketResponse> findAll(int page, int size) {
        User user=getUserInfo();
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdDate").descending());
        //;//.stream().filter(ticket -> ticket.getCategory().equals(user.getGroup().name()));
        if (user.getRole().equals(Role.ENGINEER)||user.getRole().equals(Role.MANAGER)) {
            Page<Ticket> tickets = ticketRepository.findByCategorie(Category.valueOf(user.getGroup().name()), pageable);
            return new PageResponse<>(
                    tickets.stream()
                            .map(this::mapToTicketResponse)
                            .toList(),
                    tickets.getNumber(),
                    tickets.getSize(),
                    tickets.getTotalElements(),
                    tickets.getTotalPages(),
                    tickets.isFirst(),
                    tickets.isLast()
            );
        }else {
            Page<Ticket> tickets = ticketRepository.findAll(pageable);
            return new PageResponse<>(
                    tickets.stream()
                            .map(this::mapToTicketResponse)
                            .toList(),
                    tickets.getNumber(),
                    tickets.getSize(),
                    tickets.getTotalElements(),
                    tickets.getTotalPages(),
                    tickets.isFirst(),
                    tickets.isLast()
            );
        }



    }
    @Transactional
    public String ticketAsResolved(String ticketId,String resolutionNotes) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        ticket.setResolution_notes(resolutionNotes);
        ticket.setResolution_time(LocalDateTime.now());
        ticket.setStatus(Status.RESOLVED);
        return ticketRepository.save(ticket).getId();
    }

    public void assignTicketToUser(Integer userId, String ticketId) {
        User user= userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Ticket ticket= ticketRepository.findById(ticketId).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
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
                .sender(ticket.getSender() != null ?UserLoadDTO.builder()
                        .userId(ticket.getSender().getId())
                        .fullName(ticket.getSender().fullName())
                        .email(ticket.getSender().getEmail())
                        .build(): null)
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
                        .type(comment.getType().name()!=null ? comment.getType().name() : null)
                        .creationDate(comment.getCreatedDate())
                        .user(UserLoadDTO.builder()
                                .userId(ticket.getSender().getId())
                                .fullName(ticket.getSender().fullName())
                                .email(ticket.getSender().getEmail())
                                .build())
                        .build()).toList())
                .build();
    }

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void nettingTableOfTicketsNull(){
        ticketRepository.nettingTableWhereNull();
        log.info("Netting null Tickets already");
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void notifiyingTicketsNotAssignedAfterThreeDaysOfCreation() throws MessagingException {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Ticket> tickets = ticketRepository.findAllTicketsNotAssigned(threeDaysAgo);
        for (Ticket ticket : tickets) {
            emailService.sendEmailForTicketNotAssigned(ticket.getSender(),ticket.getSender().fullName(),EmailTemplateName.TICKET_NOT_ASSIGNED,ticket,"Ticket Not Assigned After 3 days");
        }

        log.info("Netting null Tickets already");
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void makeAllTicketResolvedHas5DaysAsClosed() throws MessagingException {
       List<Ticket> tickets = ticketRepository.findAllByStatusAsResolved(LocalDateTime.now().minusDays(5));
       for (Ticket ticket : tickets) {
           ticket.setStatus(Status.CLOSED);
           ticketRepository.save(ticket);
           emailService.sendEmailForTicketNotAssigned(ticket.getSender(),ticket.getSender().fullName(),EmailTemplateName.TICKET_NOT_ASSIGNED,ticket,"Your ticket has been changed Status to Closed after 5 days of resolution");
       }
    }




    @Transactional
    public String addCommentToTicket(String ticketId,Comment comment) {
        Ticket ticket=ticketRepository.findById(ticketId).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        User user=getUserInfo();
        comment.setUser(user);
        comment=commentRepository.save(comment);
        List<Comment> comments=ticket.getComments();
        comments.add(comment);
        ticket.setComments(comments);
        return ticketRepository.save(ticket).getId();
    }


    @Scheduled(fixedRate = 300_000)
    public void processEmails() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getInstance(props, null);
        Store store = session.getStore();
        store.connect("imap.gmail.com", "ihebtbessi37@gmail.com", "*********");

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        Message[] messages = inbox.getMessages();

        for (Message message : messages) {
            if (!message.isSet(Flags.Flag.SEEN)) {
                String title = message.getSubject();
                String description = message.getContent().toString();

                Map<String, String> fields = llamaService.analyzeTicket(title, description);
                User user=userRepository.findByEmail(Arrays.toString(message.getFrom())).orElseThrow(()->new EntityNotFoundException("User not found"));

                Ticket t = new Ticket();
                t.setTitle(title);
                t.setDescription(description);
                t.setPriority(Priority.valueOf(fields.get("priority")));
                t.setStatus(Status.valueOf(fields.get("status")));
                t.setCategory(Category.valueOf(fields.get("category")));
                t.setType(TypeProbleme.valueOf(fields.get("type")));
                t.setSender(user);

                ticketRepository.save(t);

                message.setFlag(Flags.Flag.SEEN, true);
            }
        }

        inbox.close(false);
        store.close();
    }








}
