package itsm.itsm_backend.ollamaSuggestion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.http.HttpRequest;



@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaService {


    private final RestTemplate restTemplate;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:llama3.1}")
    private String model;

    public List<Double> getEmbedding(String text) {
        try {
            String url = ollamaUrl + "/api/embeddings";

            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("prompt", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Double> embedding = (List<Double>) response.getBody().get("embedding");
                return embedding;
            }

            return new ArrayList<>();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à Ollama: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public double calculateCosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size() || vectorA.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public Map<String, String> analyzeTicket(String title, String description) throws IOException, InterruptedException {
        if (isGenericEmail(title, description)) {
            System.out.println("Email détecté comme générique : " + title);
            return Map.of();
        }

        String prompt = String.format("""
        Voici un ticket avec :
        - titre : %s
        - description : %s

        Peux-tu analyser ce ticket et me retourner une réponse JSON avec les champs suivants :
        {
          "priority": "LOW|MEDIUM|HIGH|CRITICAL",
          "status": "NEW|IN_PROGRESS|ON_HOLD|CLOSED|RESOLVED",
          "category": "SOFTWARE|HARDWARE|NETWORK|OTHER",
          "type": "BUG|FEATURE"
        }
        """, title, description);

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama3");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        String jsonRequest = mapper.writeValueAsString(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String body = response.body();

        JsonNode root = mapper.readTree(body);
        JsonNode responseNode = root.get("response");
        if (responseNode == null || responseNode.isNull()) {
            throw new IllegalStateException("Champ 'response' manquant dans la réponse Llama3 : " + body);
        }

        String jsonRaw = responseNode.asText();
        int start = jsonRaw.indexOf('{');
        int end = jsonRaw.lastIndexOf('}') + 1;
        if (start == -1 || end == -1) {
            throw new IllegalStateException("Format JSON mal formé dans le texte : " + jsonRaw);
        }

        String cleanJson = jsonRaw.substring(start, end);

        return mapper.readValue(cleanJson, new TypeReference<>() {});
    }

    public boolean isGenericEmail(String title, String description) throws IOException, InterruptedException {
        String prompt = """
Tu es un assistant intelligent qui trie les emails reçus par une entreprise de support IT.

Voici un email reçu :
- Titre : %s
- Description : %s

Ta tâche est de détecter si cet email contient un **vrai ticket de support** ou non.

Considère comme **non valides** :
- Les emails promotionnels, newsletters, publicités
- Les notifications automatiques
- Les emails de type "Bonjour", "Merci", "Cordialement" sans contexte
- Les emails sans description ou trop vagues

Même si le titre est court, si le contenu décrit un problème réel ou une demande, considère-le comme **un vrai ticket**.

Réponds uniquement avec :
{"is_generic": true} → si ce n'est pas un vrai ticket
{"is_generic": false} → si c'est un vrai ticket
""".formatted(title, description);


        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama3");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        String jsonRequest = mapper.writeValueAsString(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String body = response.body();
        JsonNode root = mapper.readTree(body);
        JsonNode responseNode = root.get("response");

        if (responseNode == null || responseNode.isNull()) {
            throw new IllegalStateException("Champ 'response' manquant dans la réponse Llama3 : " + body);
        }

        String responseText = responseNode.asText().trim();
        int start = responseText.indexOf('{');
        int end = responseText.lastIndexOf('}') + 1;
        if (start == -1 || end == -1) {
            throw new IllegalStateException("Réponse mal formée : " + responseText);
        }

        String cleanJson = responseText.substring(start, end);
        JsonNode result = mapper.readTree(cleanJson);

        return result.has("is_generic") && result.get("is_generic").asBoolean();
    }




}

