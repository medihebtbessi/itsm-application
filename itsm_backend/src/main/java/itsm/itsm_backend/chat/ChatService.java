package itsm.itsm_backend.chat;

import itsm.itsm_backend.user.User;
import itsm.itsm_backend.user.UserRepository;
import itsm.itsm_backend.user.UserResponse;
import itsm.itsm_backend.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMapper mapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByReceiverId(){
         UserResponse user=this.userService.getUserInfo();
        return chatRepository.findChatsBySenderId(user.getId())
                .stream()
                .map(c->mapper.toChatResponse(c,user.getId()))
                .toList();
    }

    public String createChat(Integer senderId,Integer receiverId){
        Optional<Chat> exitingChat=chatRepository.findChatByReceiverAndSender(senderId,receiverId);
        if (exitingChat.isPresent()){
            return exitingChat.get().getId();
        }
        User sender=userRepository.findByPublicId(senderId)
                .orElseThrow(()->new EntityNotFoundException("User with id "+senderId+" not found"));

        User receiver=userRepository.findByPublicId(receiverId)
                .orElseThrow(()->new EntityNotFoundException("User with id "+receiverId+" not found"));

        Chat chat=new Chat();
        chat.setSender(sender);
        chat.setRecipient(receiver);
        Chat savedChat=chatRepository.save(chat);
        return savedChat.getId();
    }
}
