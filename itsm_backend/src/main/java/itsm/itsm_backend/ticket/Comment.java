package itsm.itsm_backend.ticket;

import itsm.itsm_backend.common.BaseAuditingEntity;
import itsm.itsm_backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Comment extends BaseAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String content;
    @Enumerated(EnumType.STRING)
    private TypeOfContent type;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
