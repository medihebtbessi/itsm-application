package itsm.itsm_backend.chatBot;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotService {
    private final RestTemplate restTemplate = new RestTemplate();

    public String askBot(String question, String context) {
        String url = "http://localhost:5000/chat";

        ChatRequest request = new ChatRequest();
        request.setContext(context);
        request.setQuestion(question);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(url, entity, ChatResponse.class);

        return response.getBody().getResponse();
    }
}
