package itsm.itsm_backend.ticket;

import itsm.itsm_backend.ws.WebSocketPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketEventConsumer {
    private final NotificationService notificationService;

    @KafkaListener(topics = "ticketserver.public.ticket", groupId = "ticket-group")
    public void consume(String message) {
        System.out.println("Received change event: " + message);
        notificationService.processAndPublishNotification(message);
    }
}
