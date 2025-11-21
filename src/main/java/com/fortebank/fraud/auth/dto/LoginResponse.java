package com.fortebank.fraud.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа после успешного логина
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private UserDTO user;
    private Long expiresIn; // Время жизни токена в секундах
}