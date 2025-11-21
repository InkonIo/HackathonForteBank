package com.fortebank.fraud.auth.service;

import com.fortebank.fraud.auth.dto.LoginRequest;
import com.fortebank.fraud.auth.dto.LoginResponse;
import com.fortebank.fraud.auth.dto.UserDTO;
import com.fortebank.fraud.auth.entity.User;
import com.fortebank.fraud.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Сервис для аутентификации пользователей
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Вход пользователя в систему
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // Аутентификация через Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            // Генерация JWT токена
            String token = tokenProvider.generateToken(authentication);
            
            // Обновить время последнего входа
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Формирование ответа
            return LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .user(UserDTO.fromEntity(user))
                    .expiresIn(tokenProvider.getExpirationInSeconds())
                    .build();
            
        } catch (AuthenticationException e) {
            throw new RuntimeException("Неверный email или пароль");
        }
    }
    
    /**
     * Регистрация нового пользователя (только для ADMIN)
     */
    @Transactional
    public UserDTO registerUser(String email, String password, String fullName, User.UserRole role) {
        // Проверка существования пользователя
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        
        // Создание нового пользователя
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .enabled(true)
                .accountNonLocked(true)
                .build();
        
        User savedUser = userRepository.save(user);
        
        return UserDTO.fromEntity(savedUser);
    }
}