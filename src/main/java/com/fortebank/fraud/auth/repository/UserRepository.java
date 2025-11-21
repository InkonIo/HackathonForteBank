package com.fortebank.fraud.auth.repository;

import com.fortebank.fraud.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Найти пользователя по email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Проверить существование пользователя по email
     */
    boolean existsByEmail(String email);
}