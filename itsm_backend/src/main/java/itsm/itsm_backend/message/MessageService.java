package itsm.itsm_backend.message;

import itsm.itsm_backend.chat.Chat;
import itsm.itsm_backend.chat.ChatRepository;
import itsm.itsm_backend.file.FileService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final FileService fileService;
    private final MessageMapper mapper;

    public void saveMessage(MessageRequest messageRequest){
        Chat chat=chatRepository.findById(messageRequest.getChatId())
                .orElseThrow(()->new EntityNotFoundException("Chat not found"));
        Message message =new Message();
        message.setContent(messageRequest.getContent());
        message.setChat(chat);
        message.setSenderId(messageRequest.getSenderId());
        message.setReceiverId(messageRequest.getReceiverId());
        message.setType(messageRequest.getType());
        message.setState(MessageState.SENT);
        messageRepository.save(message);

    }

    public List<MessageResponse> findChatMessages(String chatId){
        return messageRepository.findMessagesByChatId(chatId)
                .stream()
                .map(mapper::toMessageResponse)
                .toList();
    }

    @Transactional
    public void setMessagesToSeen(String chatId, Authentication authentication){
        Chat chat=chatRepository.findById(chatId)
                .orElseThrow(()->new EntityNotFoundException("Chat not found"));
        final Integer recipientId=getRecipientId(chat,authentication);
        messageRepository.setMessagesToSeenByChatId(chatId,MessageState.SEEN);


    }

    public void uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication){
        Chat chat=chatRepository.findById(chatId)
                .orElseThrow(()->new EntityNotFoundException("Chat not found"));

        final Integer senderId=getSenderId(chat,authentication);
        final Integer recipientId=getRecipientId(chat,authentication);

        final String filePath=fileService.saveFile(file,senderId);
        Message message =new Message();
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(recipientId);
        message.setType(MessageType.IMAGE);
        message.setState(MessageState.SENT);
        message.setMediaFilePath(filePath);
        messageRepository.save(message);



    }

    private Integer getSenderId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())){
            return chat.getSender().getId();
        }
        return chat.getRecipient().getId();
    }

    private Integer getRecipientId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())){
            return chat.getRecipient().getId();
        }
        return chat.getSender().getId();
    }
}
