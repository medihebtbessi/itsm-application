package itsm.itsm_backend.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import itsm.itsm_backend.ws.WebSocketPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebSocketPublisher publisher;

    public void processAndPublishNotification(String messageJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(messageJson);

            JsonNode afterNode = root.path("after");

            if (afterNode.isMissingNode() || afterNode.isNull()) {
                return;
            }

            Notification notification = mapper.treeToValue(afterNode, Notification.class);

            redisTemplate.opsForList().leftPush("notifications-list", notification);

            String notificationJson = mapper.writeValueAsString(notification);
            publisher.sendToAll(notificationJson);

        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing notification JSON", e);
        }
    }

}
