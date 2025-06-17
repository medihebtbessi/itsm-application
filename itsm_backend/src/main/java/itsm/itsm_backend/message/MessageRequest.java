package itsm.itsm_backend.message;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequest {

    private String content;
    private Integer senderId;
    private Integer receiverId;
    private MessageType type;
    private String chatId;
}
