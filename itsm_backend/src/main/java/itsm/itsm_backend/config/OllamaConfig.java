package itsm.itsm_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OllamaConfig {

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}