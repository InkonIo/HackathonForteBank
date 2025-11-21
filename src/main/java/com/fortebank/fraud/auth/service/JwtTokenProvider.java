package com.fortebank.fraud.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Сервис для работы с JWT токенами
 * Обновлено для JJWT 0.12.x
 */
@Service
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs; // В миллисекундах (например, 86400000 = 24 часа)
    
    /**
     * Генерация JWT токена
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // НОВЫЙ API для JJWT 0.12.x
        return Jwts.builder()
                .subject(userDetails.getUsername())  // Email пользователя (НОВЫЙ МЕТОД)
                .issuedAt(now)                       // НОВЫЙ МЕТОД
                .expiration(expiryDate)              // НОВЫЙ МЕТОД
                .signWith(key)                       // НОВЫЙ МЕТОД (без SignatureAlgorithm)
                .compact();
    }
    
    /**
     * Получить email из токена
     */
    public String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // НОВЫЙ API для парсинга
        Claims claims = Jwts.parser()              // parser() вместо parserBuilder()
                .verifyWith(key)                   // verifyWith() вместо setSigningKey()
                .build()
                .parseSignedClaims(token)          // parseSignedClaims() вместо parseClaimsJws()
                .getPayload();                     // getPayload() вместо getBody()
        
        return claims.getSubject();
    }
    
    /**
     * Валидация токена
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Токен невалидный
            return false;
        }
    }
    
    /**
     * Получить время жизни токена в секундах
     */
    public long getExpirationInSeconds() {
        return jwtExpirationMs / 1000;
    }
}