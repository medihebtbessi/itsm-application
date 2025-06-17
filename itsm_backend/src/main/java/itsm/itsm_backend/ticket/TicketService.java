package itsm.itsm_backend.ticket;

import itsm.itsm_backend.common.PageResponse;
import itsm.itsm_backend.user.User;
import itsm.itsm_backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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



@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public PageResponse<Ticket> getTicketsAsRecipient(int page, int size) {
       User user = getUserInfo();
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<Ticket> tickets = ticketRepository.getTicketsAsRecipient(pageable, user.getId());
        return new PageResponse<>(
            tickets.stream().toList(),
            tickets.getNumber(),
            tickets.getSize(),
            tickets.getTotalElements(),
            tickets.getTotalPages(),
            tickets.isFirst(),
            tickets.isLast()
        );
    }
    public PageResponse<Ticket> getTicketsAsSender(int page, int size) {
        User user = getUserInfo();
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<Ticket> tickets = ticketRepository.getTicketsAsSender(pageable, user.getId());
        return new PageResponse<>(
                tickets.stream().toList(),
                tickets.getNumber(),
                tickets.getSize(),
                tickets.getTotalElements(),
                tickets.getTotalPages(),
                tickets.isFirst(),
                tickets.isLast()
        );
    }
    public String save(Ticket ticket) {
        return ticketRepository.save(ticket).getId();
    }
    public Ticket findById(String id) {
        return ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
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
        return ticketRepository.save(ticketUpdated).getId();
    }
    public PageResponse<Ticket> findAll(int page, int size) {
        Pageable pageable= PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<Ticket> tickets = ticketRepository.findAll(pageable);
        return new PageResponse<>(
                tickets.stream().toList(),
                tickets.getNumber(),
                tickets.getSize(),
                tickets.getTotalElements(),
                tickets.getTotalPages(),
                tickets.isFirst(),
                tickets.isLast()
        );
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


}
