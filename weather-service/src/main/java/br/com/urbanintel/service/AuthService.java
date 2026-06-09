package br.com.urbanintel.service;

import br.com.urbanintel.model.AppUser;
import br.com.urbanintel.repository.UserRepository;
import br.com.urbanintel.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public String login(String username, String password) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado: " + username));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Credenciais invalidas.");
        }
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }

    @Transactional
    public String register(String username, String password, List<String> roles) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Usuario ja existe: " + username);
        }
        List<String> effectiveRoles = roles != null && !roles.isEmpty() ? roles : List.of("USER");
        AppUser user = AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(effectiveRoles)
                .build();
        userRepository.save(user);
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }
}
