package com.fortebank.fraud.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Конфигурация RestTemplate для HTTP запросов
 * 
 * Используется для:
 * - Вызова Python ML API
 * - Вызова OpenAI ChatGPT API
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * Основной RestTemplate с таймаутами
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .setConnectTimeout(Duration.ofSeconds(10))  // Timeout на подключение
                .setReadTimeout(Duration.ofSeconds(30))     // Timeout на чтение ответа
                .build();
    }
    
    /**
     * RestTemplate специально для Python ML API
     * (может иметь другие таймауты)
     */
    @Bean("mlRestTemplate")
    public RestTemplate mlRestTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))  // ML должна отвечать быстро
                .build();
    }
    
    /**
     * RestTemplate для OpenAI API
     * (может требовать больше времени на ответ)
     */
    @Bean("openAIRestTemplate")
    public RestTemplate openAIRestTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(60))  // ChatGPT может отвечать долго
                .build();
    }
    
    /**
     * Фабрика для создания HTTP клиентов
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10 секунд
        factory.setReadTimeout(30000);     // 30 секунд
        return factory;
    }
}