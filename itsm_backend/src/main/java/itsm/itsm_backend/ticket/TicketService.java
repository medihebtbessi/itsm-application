package itsm.itsm_backend.ticket;

import itsm.itsm_backend.common.PageResponse;
import itsm.itsm_backend.dashboard.UserLoadDTO;
import itsm.itsm_backend.user.User;
import itsm.itsm_backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

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
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdDate").descending());
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
                .build();
    }


}
