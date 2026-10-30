package br.com.petflow.petflow_api.security;

import br.com.petflow.petflow_api.entity.Tutor;
import br.com.petflow.petflow_api.enums.UserRole;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

/**
 * Responsável por gerar e validar os tokens JWT usados para autenticar
 * as requisições à API. O token carrega o email (subject) e o papel (role)
 * do usuário, para que o filtro de autenticação consiga reconstruir as
 * autoridades sem precisar consultar o banco a cada requisição.
 */
@Service
public class JwtService {

    static final int MIN_SECRET_LENGTH = 32;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "jwt.secret (JWT_SECRET) deve ter no mínimo " + MIN_SECRET_LENGTH + " caracteres.");
        }
    }

    public String generateToken(Tutor tutor) {
        return buildToken(tutor.getId(), tutor.getEmail(), tutor.getName(), tutor.getRole());
    }

    public String generateToken(AuthenticatedTutor tutor) {
        return buildToken(tutor.getId(), tutor.getEmail(), tutor.getName(), tutor.getRole());
    }

    public DecodedJWT validateToken(String token) {
        try {
            return JWT.require(algorithm()).build().verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String extractEmail(String token) {
        DecodedJWT decoded = validateToken(token);
        return decoded != null ? decoded.getSubject() : null;
    }

    private String buildToken(Long id, String email, String name, UserRole role) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(email)
                .withClaim("id", id)
                .withClaim("name", name)
                .withClaim("role", role.name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusMillis(expirationMs)))
                .sign(algorithm());
    }

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret);
    }
}
