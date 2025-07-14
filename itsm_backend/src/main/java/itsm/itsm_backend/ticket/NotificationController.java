package itsm.itsm_backend.ticket;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    public List<Object> getNotifications() {
        return redisTemplate.opsForList().range("notifications-list", 0, -1);
    }
}
