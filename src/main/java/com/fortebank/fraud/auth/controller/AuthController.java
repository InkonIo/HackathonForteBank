package com.fortebank.fraud.auth.controller;

import com.fortebank.fraud.auth.dto.LoginRequest;
import com.fortebank.fraud.auth.dto.LoginResponse;
import com.fortebank.fraud.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API контроллер для аутентификации
 * 
 * Endpoints:
 * POST /api/auth/login - Вход в систему
 * POST /api/auth/logout - Выход из системы
 * GET  /api/auth/me - Получить текущего пользователя
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Для разработки, потом настроить CORS правильно
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Вход в систему
     * 
     * @param request - email и пароль
     * @return JWT токен и информация о пользователе
     * 
     * Пример запроса:
     * POST /api/auth/login
     * {
     *   "email": "admin@fortebank.kz",
     *   "password": "admin123"
     * }
     * 
     * Пример ответа:
     * {
     *   "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
     *   "tokenType": "Bearer",
     *   "user": {
     *     "id": 1,
     *     "email": "admin@fortebank.kz",
     *     "fullName": "Админов Админ Админович",
     *     "role": "ADMIN",
     *     "enabled": true
     *   },
     *   "expiresIn": 86400
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Выход из системы
     * (В JWT токенах выход реализуется на клиенте - просто удалить токен)
     * Этот endpoint нужен для логирования выхода
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // TODO: Можно добавить логирование выхода пользователя
        return ResponseEntity.ok().build();
    }
    
    /**
     * Получить информацию о текущем пользователе
     * Требует авторизации (JWT токен в заголовке)
     * 
     * Заголовок: Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        // TODO: Реализовать получение текущего пользователя из SecurityContext
        return ResponseEntity.ok("Current user info");
    }
}