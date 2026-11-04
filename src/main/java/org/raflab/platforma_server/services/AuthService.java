package org.raflab.platforma_server.services;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.config.SecurityConfig;
import org.raflab.platforma_server.config.SecurityProperties;
import org.raflab.platforma_server.exceptions.UnauthorizedException;
import org.raflab.platforma_server.model.User;
import org.raflab.platforma_server.repository.UserRepository;
import org.raflab.platforma_server.model.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    public record LoginResult(String token, Instant expiresAt, Role role) {
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final SecurityProperties securityProperties;

    @Transactional(readOnly = true)
    public LoginResult login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(securityProperties.jwtDuration());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("platforma-server")
                .subject(user.getUsername())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(SecurityConfig.ROLE_CLAIM, user.getRole().name())
                .claim("userId", user.getId())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResult(token, expiresAt, user.getRole());
    }
}
