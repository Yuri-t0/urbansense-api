package br.com.urbanintel.infrastructure.adapter.in.web;

import br.com.urbanintel.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticacao JWT")
public class AuthWebAdapter {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Retorna token JWT para autenticacao")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    @Operation(summary = "Registro", description = "Registra novo usuario e retorna token JWT")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        String token = authService.register(request.username(), request.password(), request.roles());
        return ResponseEntity.status(201).body(Map.of("token", token));
    }

    record RegisterRequest(String username, String password, List<String> roles) {}
    record LoginRequest(String username, String password) {}
}
