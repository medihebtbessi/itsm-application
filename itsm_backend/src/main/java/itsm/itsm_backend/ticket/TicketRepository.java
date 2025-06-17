package itsm.itsm_backend.ticket;

import itsm.itsm_backend.dashboard.UserLoadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,String>, JpaSpecificationExecutor<Ticket> {
    @Query("select t from Ticket t where t.recipient.id= :userId")
    Page<Ticket> getTicketsAsRecipient(Pageable pageable,@Param("userId") Integer userId);
    @Query("select t from Ticket t where t.sender.id= :userId")
    Page<Ticket> getTicketsAsSender(Pageable pageable,@Param("userId") Integer userId);
    /*@Query("select t from Ticket t where t.sender.id= :id")
    Page<Ticket> findBySender(Pageable pageable,@Param("id") Integer id);*/
    @Query("""
       SELECT new itsm.itsm_backend.dashboard.UserLoadDTO(
               u.id,
               CONCAT(u.firstname, ' ', u.lastname),
               COUNT(t)
       )
       FROM Ticket t
       JOIN t.recipient u
       GROUP BY u.id, u.firstname, u.lastname
       """)
    List<UserLoadDTO> getLoadByRecipient();
}
