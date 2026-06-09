package br.com.urbanintel.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider - Testes Unitários")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "urban-intel-super-secret-key-test-must-be-long-enough-256bits");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 86400000L);
    }

    @Test
    @DisplayName("Deve gerar token válido e extrair username corretamente")
    void generateToken_shouldProduceValidToken() {
        String token = tokenProvider.generateToken("joao", List.of("USER"));

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.extractUsername(token)).isEqualTo("joao");
    }

    @Test
    @DisplayName("Deve extrair roles do token")
    void generateToken_shouldContainRoles() {
        String token = tokenProvider.generateToken("admin", List.of("ADMIN", "USER"));

        List<String> roles = tokenProvider.extractRoles(token);
        assertThat(roles).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("Token gerado deve ser válido")
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = tokenProvider.generateToken("maria", List.of("USER"));
        assertThat(tokenProvider.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Token adulterado deve ser inválido")
    void isTokenValid_shouldReturnFalseForTamperedToken() {
        String token = tokenProvider.generateToken("hacker", List.of("ADMIN"));
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(tokenProvider.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("Token expirado deve ser inválido")
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "jwtSecret",
                "urban-intel-super-secret-key-test-must-be-long-enough-256bits");
        ReflectionTestUtils.setField(expiredProvider, "jwtExpiration", -1000L);

        String token = expiredProvider.generateToken("expired", List.of("USER"));
        assertThat(expiredProvider.isTokenValid(token)).isFalse();
    }
}
