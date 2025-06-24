package itsm.itsm_backend.ticket;

import itsm.itsm_backend.common.BaseAuditingEntity;
import itsm.itsm_backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Ticket extends BaseAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private TypeProbleme type;

    private String resolution_notes;
    private LocalDateTime resolution_time;

    // Simplified embedding storage - use only one approach
   /* @Column(name = "embedding", columnDefinition = "vector(1024)")
    private String embeddingVector;

    // Transient field for easier manipulation
    @Transient
    private float[] embedding;*/

    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    private User recipient;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attachment> attachments;

    // Utility methods for embedding conversion
   /* public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
        this.embeddingVector = arrayToVectorString(embedding);
    }

    public float[] getEmbedding() {
        if (embedding == null && embeddingVector != null) {
            embedding = vectorStringToArray(embeddingVector);
        }
        return embedding;
    }

    public void setEmbeddingVector(String embeddingVector) {
        this.embeddingVector = embeddingVector;
        this.embedding = null; // Reset cache
    }

    private String arrayToVectorString(float[] array) {
        if (array == null || array.length == 0) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private float[] vectorStringToArray(String vectorStr) {
        if (vectorStr == null || vectorStr.trim().isEmpty()) return null;

        // Remove brackets and split
        vectorStr = vectorStr.trim();
        if (vectorStr.startsWith("[") && vectorStr.endsWith("]")) {
            vectorStr = vectorStr.substring(1, vectorStr.length() - 1);
        }

        if (vectorStr.trim().isEmpty()) return null;

        String[] parts = vectorStr.split(",");
        float[] result = new float[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Float.parseFloat(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid vector format: " + vectorStr, e);
            }
        }
        return result;
    }

    // Helper method to check if embedding exists
    public boolean hasEmbedding() {
        return embeddingVector != null && !embeddingVector.trim().isEmpty();
    }*/
}