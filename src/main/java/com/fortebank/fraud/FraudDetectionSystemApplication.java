package com.fortebank.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class FraudDetectionSystemApplication {

	public static void main(String[] args) {
        // Этот код загрузит переменные из .env в System.getenv()
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
        
        SpringApplication.run(FraudDetectionSystemApplication.class, args);
    }

}
