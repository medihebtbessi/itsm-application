package itsm.itsm_backend.chat;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@Tag(name = "Chat")
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<StringResponse> createChat(@RequestParam(name = "sender-id") Integer senderId,
                                                     @RequestParam(name = "receiver-id") Integer receiverId){

        final String chatId= chatService.createChat(senderId,receiverId);
        StringResponse stringResponse=StringResponse.builder()
                .response(chatId)
                .build();
        return ResponseEntity.ok(stringResponse);
    }

    @GetMapping
    public ResponseEntity<List<ChatResponse>> getChatsByReceiver(){
        return ResponseEntity.ok(chatService.getChatsByReceiverId());
    }
}