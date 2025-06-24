package itsm.itsm_backend.ticket;

import itsm.itsm_backend.dashboard.UserLoadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    List<Ticket> findActiveTicketsExcluding(@Param("excludeId") Long excludeId);
    /**
     * Find similar tickets using cosine distance with threshold
     * Lower distance values indicate higher similarity

    @Query(value = """
        SELECT t.*, 
               (t.embedding <=> CAST(:embeddingStr AS vector)) AS distance
        FROM ticket t
        WHERE t.status = 'RESOLVED' 
        AND t.embedding IS NOT NULL
        AND (t.embedding <=> CAST(:embeddingStr AS vector)) <= :threshold
        ORDER BY (t.embedding <=> CAST(:embeddingStr AS vector)) ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<Ticket> findTopKSimilarWithThreshold(
            @Param("embeddingStr") String embeddingStr,
            @Param("threshold") double threshold,
            @Param("limit") int limit
    );

    /**
     * Find similar tickets excluding very close matches (likely duplicates)
     * Useful when you want to exclude the same ticket or near-duplicates

    @Query(value = """
        SELECT t.*, 
               (t.embedding <=> CAST(:embeddingStr AS vector)) AS distance
        FROM ticket t
        WHERE t.status = 'RESOLVED' 
        AND t.embedding IS NOT NULL
        AND (t.embedding <=> CAST(:embeddingStr AS vector)) > :minDistance
        AND (t.embedding <=> CAST(:embeddingStr AS vector)) <= :maxDistance
        ORDER BY (t.embedding <=> CAST(:embeddingStr AS vector)) ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<Ticket> findSimilarWithinRange(
            @Param("embeddingStr") String embeddingStr,
            @Param("minDistance") double minDistance,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit
    );

    /**
     * Find top K similar tickets excluding self (if the same ticket)

    @Query(value = """
        SELECT t.*, 
               (t.embedding <=> CAST(:embeddingStr AS vector)) AS distance
        FROM ticket t
        WHERE t.status = 'RESOLVED' 
        AND t.embedding IS NOT NULL
        AND t.id != :excludeId
        AND (t.embedding <=> CAST(:embeddingStr AS vector)) > 0.01
        ORDER BY (t.embedding <=> CAST(:embeddingStr AS vector)) ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<Ticket> findTopKSimilarExcludingTicket(
            @Param("embeddingStr") String embeddingStr,
            @Param("excludeId") String excludeId,
            @Param("limit") int limit
    );

    /**
     * Get similarity distribution for analysis

    @Query(value = """
        SELECT 
            CASE 
                WHEN (embedding <=> CAST(:embeddingStr AS vector)) <= 0.3 THEN 'HIGH'
                WHEN (embedding <=> CAST(:embeddingStr AS vector)) <= 0.6 THEN 'MEDIUM'
                WHEN (embedding <=> CAST(:embeddingStr AS vector)) <= 0.8 THEN 'LOW'
                ELSE 'VERY_LOW'
            END as similarity_level,
            COUNT(*) as count
        FROM ticket
        WHERE status = 'RESOLVED' 
        AND embedding IS NOT NULL
        GROUP BY similarity_level
    """, nativeQuery = true)
    List<Object[]> getSimilarityDistribution(@Param("embeddingStr") String embeddingStr);

    // Standard queries
    List<Ticket> findAllByStatus(Status status);

    /**
     * Find tickets by status without embeddings (for bulk processing)

    @Query("SELECT t FROM Ticket t WHERE t.status = :status AND t.embeddingVector IS NULL")
    List<Ticket> findByStatusAndEmbeddingVectorIsNull(Status status, Pageable pageable);

    /**
     * Count tickets by status without embeddings

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status AND t.embeddingVector IS NULL")
    long countByStatusAndEmbeddingVectorIsNull(Status status);


    @Query("SELECT t FROM Ticket t WHERE t.status = 'RESOLVED' AND t.embeddingVector IS NULL")
    List<Ticket> findResolvedTicketsWithoutEmbeddings(Pageable pageable);


    @Query(value = """
        SELECT t.*, 
               (t.embedding <=> CAST(:embeddingStr AS vector)) AS distance
        FROM ticket t
        WHERE t.status = 'RESOLVED' 
        AND t.embedding IS NOT NULL
        AND (t.embedding <=> CAST(:embeddingStr AS vector)) <= :threshold
        AND (:category IS NULL OR t.category = :category)
        AND (:priority IS NULL OR t.priority = :priority)
        ORDER BY (t.embedding <=> CAST(:embeddingStr AS vector)) ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<Ticket> findSimilarWithFilters(
            @Param("embeddingStr") String embeddingStr,
            @Param("threshold") double threshold,
            @Param("category") String category,
            @Param("priority") String priority,
            @Param("limit") int limit
    );


    @Query(value = """
        WITH ticket_pairs AS (
            SELECT 
                t1.id as ticket1_id,
                t2.id as ticket2_id,
                (t1.embedding <=> t2.embedding) as distance
            FROM ticket t1
            JOIN ticket t2 ON t1.id < t2.id
            WHERE t1.embedding IS NOT NULL 
            AND t2.embedding IS NOT NULL
            AND t1.status = 'RESOLVED'
            AND t2.status = 'RESOLVED'
            AND (t1.embedding <=> t2.embedding) <= :duplicateThreshold
        )
        SELECT t.*, tp.distance
        FROM ticket_pairs tp
        JOIN ticket t ON (t.id = tp.ticket1_id OR t.id = tp.ticket2_id)
        ORDER BY tp.distance ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<Ticket> findPotentialDuplicates(
            @Param("duplicateThreshold") double duplicateThreshold,
            @Param("limit") int limit
    );*/
}