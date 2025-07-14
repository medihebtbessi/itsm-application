package itsm.itsm_backend.ticket.jpa;

import itsm.itsm_backend.ticket.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment,Long> {
}
