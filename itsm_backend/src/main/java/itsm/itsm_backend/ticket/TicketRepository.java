package itsm.itsm_backend.ticket;

import itsm.itsm_backend.dashboard.UserLoadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, String>, JpaSpecificationExecutor<Ticket> {

    @Query("select t from Ticket t where t.recipient.id = :userId")
    Page<Ticket> getTicketsAsRecipient(Pageable pageable, @Param("userId") Integer userId);

    @Query("select t from Ticket t where t.sender.id = :userId")
    Page<Ticket> getTicketsAsSender(Pageable pageable, @Param("userId") Integer userId);

    @Query("""
       SELECT new itsm.itsm_backend.dashboard.UserLoadDTO(
               u.id,
               CONCAT(u.firstname, ' ', u.lastname),
               COUNT(t),
                      u.email
       )
       FROM Ticket t
       JOIN t.recipient u
       GROUP BY u.id, u.firstname, u.lastname
       """)
    List<UserLoadDTO> getLoadByRecipient();

    @Query("SELECT t FROM Ticket t WHERE t.status != 'CLOSED' ORDER BY t.createdDate DESC")
    List<Ticket> findActiveTickets();

    @Query("SELECT t FROM Ticket t WHERE t.id != :excludeId AND t.status != 'CLOSED'")
    List<Ticket> findActiveTicketsExcluding(@Param("excludeId") String excludeId);

    @Query("select t from Ticket t where t.category = :category")
    Page<Ticket> findByCategorie(Category category, Pageable pageable);
    @Modifying
    @Query("delete Ticket t where t.title = '' and t.description = '' ")
    void nettingTableWhereNull();
    @Query("SELECT t FROM Ticket t WHERE t.createdDate <= :limitDate AND t.recipient IS NULL")
    List<Ticket> findAllTicketsNotAssigned(@Param("limitDate") LocalDateTime limitDate);
    @Query("select t from Ticket t where t.status = 'RESOLVED' and t.resolution_time <= :now")
    List<Ticket> findAllByStatusAsResolved(LocalDateTime now);



}