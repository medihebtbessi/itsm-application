package itsm.itsm_backend.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoadDTO {
        private Integer userId;
        private String fullName;
        private Long ticketCount;
}
