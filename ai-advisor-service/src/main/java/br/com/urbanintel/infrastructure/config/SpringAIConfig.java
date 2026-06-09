package br.com.urbanintel.infrastructure.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                    Você é o Assistente Urbano da plataforma Urban Intel.
                    Responda sempre em português brasileiro.
                    Seja preciso e use dados reais quando disponíveis.
                    """)
                .build();
    }
}
