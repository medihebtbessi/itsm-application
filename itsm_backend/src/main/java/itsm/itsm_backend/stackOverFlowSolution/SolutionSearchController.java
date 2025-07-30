package itsm.itsm_backend.stackOverFlowSolution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solutions")
public class SolutionSearchController {

    private static final Logger logger = LoggerFactory.getLogger(SolutionSearchController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/search")
    public ResponseEntity<List<Map<String, String>>> searchSolutions(@RequestBody TicketDto ticketDto) {
        try {
            logger.info("Recherche de solutions pour le ticket: {}", ticketDto.getTitle());

            String generatedQuestion = generateQuestionWithOllama(ticketDto.getTitle(), ticketDto.getDescription());
            logger.info("Question générée par Ollama: {}", generatedQuestion);

            if (generatedQuestion == null || generatedQuestion.trim().isEmpty()) {
                logger.warn("Aucune question générée par Ollama, utilisation de mots-clés extraits du titre");
                generatedQuestion = extractKeywordsFromTitle(ticketDto.getTitle());
            }

            List<Map<String, String>> results = searchStackOverflow(generatedQuestion);
            logger.info("Nombre de résultats trouvés: {}", results.size());

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de solutions", e);
            return ResponseEntity.ok(new ArrayList<>()); // Retourne une liste vide en cas d'erreur
        }
    }

    private String generateQuestionWithOllama(String title, String description) {
        try {
            String prompt = "Extract only the main technical keywords from this IT ticket for a search query. " +
                    "Return only 3-6 relevant technical terms separated by spaces, no formatting, no explanation:\n" +
                    "Title: " + title + "\nDescription: " + description;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama3");
            body.put("prompt", prompt);
            body.put("stream", false);
            body.put("options", Map.of("temperature", 0.1, "max_tokens", 50)); // Limite les tokens

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            logger.debug("Envoi de la requête à Ollama: {}", prompt);
            String response = restTemplate.postForObject("http://localhost:11434/api/generate", request, String.class);
            logger.debug("Réponse brute d'Ollama: {}", response);

            String generatedKeywords = extractGeneratedText(response);

            // Nettoyer et limiter la longueur
            generatedKeywords = cleanAndLimitQuery(generatedKeywords);

            return generatedKeywords;
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de question avec Ollama", e);
            return extractKeywordsFromTitle(title); // Fallback plus intelligent
        }
    }

    private String cleanAndLimitQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        // Supprimer le formatage markdown et les caractères spéciaux
        query = query.replaceAll("\\*+", "")
                .replaceAll("#+", "")
                .replaceAll("\\[.*?\\]", "")
                .replaceAll("[^a-zA-Z0-9\\s+\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // Limiter à 100 caractères max
        if (query.length() > 100) {
            query = query.substring(0, 100).trim();
        }

        logger.info("Requête nettoyée: '{}'", query);
        return query;
    }

    private String extractKeywordsFromTitle(String title) {
        // Extraction simple de mots-clés du titre comme fallback
        return title.replaceAll("[^a-zA-Z0-9\\s+\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .substring(0, Math.min(title.length(), 50));
    }

    private String extractGeneratedText(String response) {
        try {
            JsonNode jsonResponse = objectMapper.readTree(response);
            String generatedText = jsonResponse.get("response").asText().trim();
            logger.debug("Texte extrait: {}", generatedText);
            return generatedText;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction du texte généré", e);
            return "";
        }
    }

    private List<Map<String, String>> searchStackOverflow(String question) {
        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Amélioration de la requête Stack Overflow
            String encodedQuestion = URLEncoder.encode(question, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.stackexchange.com/2.3/search/advanced?" +
                            "order=desc&sort=relevance&q=%s&accepted=True&site=stackoverflow&pagesize=10&filter=default",
                    encodedQuestion
            );

            logger.info("URL de recherche Stack Overflow: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            logger.debug("Réponse brute Stack Overflow: {}", response);

            if (response == null || response.trim().isEmpty()) {
                logger.warn("Réponse vide de l'API Stack Overflow");
                return results;
            }

            JsonNode rootNode = objectMapper.readTree(response);

            // Vérifier s'il y a des erreurs dans la réponse
            if (rootNode.has("error_message")) {
                logger.error("Erreur API Stack Overflow: {}", rootNode.get("error_message").asText());
                return results;
            }

            JsonNode items = rootNode.get("items");
            if (items == null || !items.isArray()) {
                logger.warn("Aucun élément 'items' trouvé dans la réponse");
                return results;
            }

            logger.info("Nombre d'éléments dans la réponse: {}", items.size());

            for (JsonNode item : items) {
                Map<String, String> resultMap = new HashMap<>();

                String title = item.has("title") ? item.get("title").asText() : "Titre non disponible";
                String link = item.has("link") ? item.get("link").asText() : "";
                String score = item.has("score") ? String.valueOf(item.get("score").asInt()) : "0";
                String answerCount = item.has("answer_count") ? String.valueOf(item.get("answer_count").asInt()) : "0";

                resultMap.put("title", title);
                resultMap.put("link", link);
                resultMap.put("score", score);
                resultMap.put("answer_count", answerCount);

                results.add(resultMap);
                logger.debug("Ajout du résultat: {}", title);
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la recherche Stack Overflow", e);
        }

        return results;
    }
}

/*
* package itsm.itsm_backend.stackOverFlowSolution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/solutions")
public class SolutionSearchController {

    private static final Logger logger = LoggerFactory.getLogger(SolutionSearchController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    // Enum pour les plateformes supportées
    public enum SearchPlatform {
        STACKOVERFLOW, MEDIUM, GITHUB
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchSolutions(
            @RequestBody TicketDto ticketDto,
            @RequestParam(defaultValue = "STACKOVERFLOW,MEDIUM,GITHUB") Set<SearchPlatform> platforms) {

        try {
            logger.info("Recherche de solutions pour le ticket: {}", ticketDto.getTitle());
            logger.info("Plateformes sélectionnées: {}", platforms);

            String generatedQuestion = generateQuestionWithOllama(ticketDto.getTitle(), ticketDto.getDescription());
            logger.info("Question générée par Ollama: {}", generatedQuestion);

            if (generatedQuestion == null || generatedQuestion.trim().isEmpty()) {
                logger.warn("Aucune question générée par Ollama, utilisation de mots-clés extraits du titre");
                generatedQuestion = extractKeywordsFromTitle(ticketDto.getTitle());
            }

            // Recherche parallèle sur toutes les plateformes
            Map<String, Object> allResults = searchAllPlatforms(generatedQuestion, platforms);

            logger.info("Recherche terminée sur toutes les plateformes");
            return ResponseEntity.ok(allResults);

        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de solutions", e);
            return ResponseEntity.ok(createEmptyResponse());
        }
    }

    private Map<String, Object> searchAllPlatforms(String query, Set<SearchPlatform> platforms) {
        List<CompletableFuture<Map.Entry<String, List<Map<String, String>>>>> futures = new ArrayList<>();

        // Lancer les recherches en parallèle
        for (SearchPlatform platform : platforms) {
            CompletableFuture<Map.Entry<String, List<Map<String, String>>>> future =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            List<Map<String, String>> results = searchOnPlatform(platform, query);
                            return new AbstractMap.SimpleEntry<>(platform.name().toLowerCase(), results);
                        } catch (Exception e) {
                            logger.error("Erreur lors de la recherche sur {}", platform, e);
                            return new AbstractMap.SimpleEntry<>(platform.name().toLowerCase(), new ArrayList<>());
                        }
                    }, executor);
            futures.add(future);
        }

        // Collecter tous les résultats
        Map<String, Object> allResults = new HashMap<>();
        Map<String, List<Map<String, String>>> platformResults = new HashMap<>();
        int totalResults = 0;

        for (CompletableFuture<Map.Entry<String, List<Map<String, String>>>> future : futures) {
            try {
                Map.Entry<String, List<Map<String, String>>> entry = future.get();
                platformResults.put(entry.getKey(), entry.getValue());
                totalResults += entry.getValue().size();
            } catch (Exception e) {
                logger.error("Erreur lors de la récupération des résultats", e);
            }
        }

        allResults.put("results", platformResults);
        allResults.put("totalResults", totalResults);
        allResults.put("query", query);
        allResults.put("timestamp", System.currentTimeMillis());

        return allResults;
    }

    private List<Map<String, String>> searchOnPlatform(SearchPlatform platform, String query) {
        switch (platform) {
            case STACKOVERFLOW:
                return searchStackOverflow(query);
            case MEDIUM:
                return searchMedium(query);
            case GITHUB:
                return searchGitHub(query);
            default:
                return new ArrayList<>();
        }
    }

    private String generateQuestionWithOllama(String title, String description) {
        try {
            String prompt = "Extract only the main technical keywords from this IT ticket for a search query. " +
                    "Return only 3-6 relevant technical terms separated by spaces, no formatting, no explanation:\n" +
                    "Title: " + title + "\nDescription: " + description;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama3");
            body.put("prompt", prompt);
            body.put("stream", false);
            body.put("options", Map.of("temperature", 0.1, "max_tokens", 50));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            logger.debug("Envoi de la requête à Ollama: {}", prompt);
            String response = restTemplate.postForObject("http://localhost:11434/api/generate", request, String.class);
            logger.debug("Réponse brute d'Ollama: {}", response);

            String generatedKeywords = extractGeneratedText(response);
            generatedKeywords = cleanAndLimitQuery(generatedKeywords);

            return generatedKeywords;
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de question avec Ollama", e);
            return extractKeywordsFromTitle(title);
        }
    }

    private String cleanAndLimitQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        query = query.replaceAll("\\*+", "")
                .replaceAll("#+", "")
                .replaceAll("\\[.*?\\]", "")
                .replaceAll("[^a-zA-Z0-9\\s+\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (query.length() > 100) {
            query = query.substring(0, 100).trim();
        }

        logger.info("Requête nettoyée: '{}'", query);
        return query;
    }

    private String extractKeywordsFromTitle(String title) {
        return title.replaceAll("[^a-zA-Z0-9\\s+\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .substring(0, Math.min(title.length(), 50));
    }

    private String extractGeneratedText(String response) {
        try {
            JsonNode jsonResponse = objectMapper.readTree(response);
            String generatedText = jsonResponse.get("response").asText().trim();
            logger.debug("Texte extrait: {}", generatedText);
            return generatedText;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction du texte généré", e);
            return "";
        }
    }

    // Recherche Stack Overflow (votre code existant)
    private List<Map<String, String>> searchStackOverflow(String question) {
        List<Map<String, String>> results = new ArrayList<>();

        try {
            String encodedQuestion = URLEncoder.encode(question, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.stackexchange.com/2.3/search/advanced?" +
                            "order=desc&sort=relevance&q=%s&accepted=True&site=stackoverflow&pagesize=10&filter=default",
                    encodedQuestion
            );

            logger.info("URL de recherche Stack Overflow: {}", url);

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.trim().isEmpty()) {
                logger.warn("Réponse vide de l'API Stack Overflow");
                return results;
            }

            JsonNode rootNode = objectMapper.readTree(response);

            if (rootNode.has("error_message")) {
                logger.error("Erreur API Stack Overflow: {}", rootNode.get("error_message").asText());
                return results;
            }

            JsonNode items = rootNode.get("items");
            if (items == null || !items.isArray()) {
                logger.warn("Aucun élément 'items' trouvé dans la réponse Stack Overflow");
                return results;
            }

            for (JsonNode item : items) {
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("platform", "stackoverflow");
                resultMap.put("title", item.has("title") ? item.get("title").asText() : "Titre non disponible");
                resultMap.put("link", item.has("link") ? item.get("link").asText() : "");
                resultMap.put("score", item.has("score") ? String.valueOf(item.get("score").asInt()) : "0");
                resultMap.put("answer_count", item.has("answer_count") ? String.valueOf(item.get("answer_count").asInt()) : "0");
                resultMap.put("tags", extractTags(item));

                results.add(resultMap);
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la recherche Stack Overflow", e);
        }

        logger.info("Stack Overflow - {} résultats trouvés", results.size());
        return results;
    }

    // Recherche Medium via Google Search API (ou Scraping léger)
    private List<Map<String, String>> searchMedium(String query) {
        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Utilisation de Google Custom Search API ou DuckDuckGo
            String encodedQuery = URLEncoder.encode(query + " site:medium.com", StandardCharsets.UTF_8);

            // Pour cet exemple, on utilise une approche simplifiée
            // En production, vous devriez utiliser une API comme Google Custom Search
            String url = "https://api.duckduckgo.com/?q=" + encodedQuery + "&format=json&no_html=1&skip_disambig=1";

            logger.info("URL de recherche Medium: {}", url);

            String response = restTemplate.getForObject(url, String.class);

            if (response != null && !response.trim().isEmpty()) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode relatedTopics = rootNode.get("RelatedTopics");

                if (relatedTopics != null && relatedTopics.isArray()) {
                    int count = 0;
                    for (JsonNode topic : relatedTopics) {
                        if (count >= 5) break; // Limiter à 5 résultats

                        String text = topic.has("Text") ? topic.get("Text").asText() : "";
                        String firstURL = topic.has("FirstURL") ? topic.get("FirstURL").asText() : "";

                        if (firstURL.contains("medium.com") && !text.isEmpty()) {
                            Map<String, String> resultMap = new HashMap<>();
                            resultMap.put("platform", "medium");
                            resultMap.put("title", text.length() > 100 ? text.substring(0, 100) + "..." : text);
                            resultMap.put("link", firstURL);
                            resultMap.put("type", "article");

                            results.add(resultMap);
                            count++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la recherche Medium", e);
        }

        logger.info("Medium - {} résultats trouvés", results.size());
        return results;
    }

    // Recherche GitHub
    private List<Map<String, String>> searchGitHub(String query) {
        List<Map<String, String>> results = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.github.com/search/repositories?q=%s&sort=stars&order=desc&per_page=10",
                    encodedQuery
            );

            logger.info("URL de recherche GitHub: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github.v3+json");
            headers.set("User-Agent", "ITSM-Solution-Search/1.0");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);

            if (response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode items = rootNode.get("items");

                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        Map<String, String> resultMap = new HashMap<>();
                        resultMap.put("platform", "github");
                        resultMap.put("title", item.has("full_name") ? item.get("full_name").asText() : "Repository");
                        resultMap.put("link", item.has("html_url") ? item.get("html_url").asText() : "");
                        resultMap.put("description", item.has("description") && !item.get("description").isNull() ?
                                item.get("description").asText() : "Pas de description");
                        resultMap.put("stars", item.has("stargazers_count") ? String.valueOf(item.get("stargazers_count").asInt()) : "0");
                        resultMap.put("language", item.has("language") && !item.get("language").isNull() ?
                                item.get("language").asText() : "N/A");
                        resultMap.put("updated", item.has("updated_at") ? item.get("updated_at").asText() : "");

                        results.add(resultMap);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la recherche GitHub", e);
        }

        logger.info("GitHub - {} résultats trouvés", results.size());
        return results;
    }

    private String extractTags(JsonNode item) {
        if (item.has("tags") && item.get("tags").isArray()) {
            List<String> tags = new ArrayList<>();
            for (JsonNode tag : item.get("tags")) {
                tags.add(tag.asText());
            }
            return String.join(", ", tags);
        }
        return "";
    }

    private Map<String, Object> createEmptyResponse() {
        Map<String, Object> emptyResponse = new HashMap<>();
        emptyResponse.put("results", new HashMap<>());
        emptyResponse.put("totalResults", 0);
        emptyResponse.put("query", "");
        emptyResponse.put("timestamp", System.currentTimeMillis());
        return emptyResponse;
    }

    // Endpoint pour rechercher sur une plateforme spécifique
    @PostMapping("/search/{platform}")
    public ResponseEntity<List<Map<String, String>>> searchSpecificPlatform(
            @PathVariable SearchPlatform platform,
            @RequestBody TicketDto ticketDto) {

        try {
            String generatedQuestion = generateQuestionWithOllama(ticketDto.getTitle(), ticketDto.getDescription());
            if (generatedQuestion == null || generatedQuestion.trim().isEmpty()) {
                generatedQuestion = extractKeywordsFromTitle(ticketDto.getTitle());
            }

            List<Map<String, String>> results = searchOnPlatform(platform, generatedQuestion);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Erreur lors de la recherche sur {}", platform, e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}
* */