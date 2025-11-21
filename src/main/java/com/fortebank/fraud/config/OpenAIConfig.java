package com.fortebank.fraud.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для OpenAI ChatGPT API
 */
@Configuration
@Getter
public class OpenAIConfig {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.api.url}")
    private String apiUrl;
    
    @Value("${openai.api.model}")
    private String model;
    
    @Value("${openai.api.timeout}")
    private int timeout;
    
    /**
     * Проверка наличия API ключа при старте приложения
     */
    public boolean isConfigured() {
        return apiKey != null 
            && !apiKey.isEmpty() 
            && !apiKey.startsWith("sk-your-api-key");
    }
}