package itsm.itsm_backend.ticket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import itsm.itsm_backend.dashboard.UserLoadDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {

    private Integer id;
    private String content;
    private String type;
    private LocalDateTime creationDate;
    private UserLoadDTO user;

}
