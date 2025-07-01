package itsm.itsm_backend.chatBot;

import lombok.Data;

@Data
public class ChatRequest {
    private String context;
    private String question;


}