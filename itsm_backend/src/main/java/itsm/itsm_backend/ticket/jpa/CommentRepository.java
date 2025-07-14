package itsm.itsm_backend.ticket.jpa;

import itsm.itsm_backend.ticket.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Integer> {
}
