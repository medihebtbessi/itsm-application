package itsm.itsm_backend.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendToAll(String payload) {
        messagingTemplate.convertAndSend("/topic/ticket-changes", payload);
    }
}
