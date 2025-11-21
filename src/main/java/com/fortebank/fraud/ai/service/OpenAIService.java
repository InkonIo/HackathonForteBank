package com.fortebank.fraud.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.api.url}")
    private String apiUrl;
    
    @Value("${openai.api.model:gpt-4}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Вызвать ChatGPT API
     */
    public String callChatGPT(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", 
                           "Ты эксперт по противодействию мошенничеству в банке. " +
                           "Отвечай на русском языке, кратко и понятно."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 800);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = 
                        (List<Map<String, Object>>) response.getBody().get("choices");
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = 
                            (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            
            return "Не удалось получить ответ от AI";
            
        } catch (Exception e) {
            log.error("Ошибка вызова ChatGPT: {}", e.getMessage(), e);
            return "Ошибка анализа AI: " + e.getMessage();
        }
    }
}
