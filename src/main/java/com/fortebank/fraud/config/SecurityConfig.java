package com.fortebank.fraud.config;

import com.fortebank.fraud.auth.security.CustomUserDetailsService;
import com.fortebank.fraud.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация Spring Security
 * 
 * Настраивает:
 * - JWT аутентификацию
 * - CORS
 * - Доступ к endpoints
 * - Шифрование паролей
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;
    
    // Массив публичных путей для Swagger/OpenAPI
    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-resources"
    };

    /**
     * Главная конфигурация Security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Отключаем CSRF (не нужен для REST API с JWT)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Настройка CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Настройка доступа к endpoints
            .authorizeHttpRequests(auth -> auth
                // Публичные endpoints (без авторизации)
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                
                // Разрешаем доступ к Swagger UI и API Docs
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                
                // Все остальные endpoints требуют авторизации
                .anyRequest().authenticated()
            )
            
            // Stateless сессии (не храним сессии на сервере)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Добавляем наш JWT фильтр
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Настройка CORS (Cross-Origin Resource Sharing)
     * Позволяет фронтенду на другом домене делать запросы
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Разрешенные origins (фронтенд)
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",  // React dev server
            "http://localhost:5173",  // Vite dev server
            "https://fraud.fortebank.kz"  // Production
        ));
        
        // Разрешенные HTTP методы
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Разрешенные заголовки
        configuration.setAllowedHeaders(List.of("*"));
        
        // Разрешить отправку credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Какие заголовки можно читать на фронтенде
        configuration.setExposedHeaders(List.of("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Authentication Provider
     * Настраивает как Spring Security будет проверять пользователей
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    
    /**
     * Authentication Manager
     * Нужен для ручной аутентификации в AuthService
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Password Encoder
     * BCrypt - надежный алгоритм шифрования паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 12 rounds (рекомендуется)
    }
}
