package itsm.itsm_backend.message;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private String content;
    private MessageType type;
    private MessageState state;
    private Integer senderId;
    private Integer receiverId;
    private LocalDateTime createdAt;
    private byte[] media;

}
