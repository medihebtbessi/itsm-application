package itsm.itsm_backend.ticket;

import itsm.itsm_backend.file.ImageService;
import itsm.itsm_backend.ticket.jpa.AttachmentRepository;
import itsm.itsm_backend.ticket.jpa.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final ImageService imageService;
    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;

    public Long saveImage(String ticketId, MultipartFile file) throws IOException {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(()->new EntityNotFoundException("Ticket not found"));
        String filename = file.getOriginalFilename();
        String url=imageService.upload(file);
        Attachment attachment=Attachment.builder()
                .filename(filename)
                .url(url)
                .build();
        attachment.setTicket(ticket);
        return attachmentRepository.save(attachment).getId();
    }


}
