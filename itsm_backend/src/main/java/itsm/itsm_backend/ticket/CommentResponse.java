package itsm.itsm_backend.ticket;

import itsm.itsm_backend.dashboard.UserLoadDTO;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Builder
@Data
public class CommentResponse {

    private Integer id;
    private String content;
    private String type;
    private LocalDateTime creationDate;
    private UserLoadDTO user;

}
