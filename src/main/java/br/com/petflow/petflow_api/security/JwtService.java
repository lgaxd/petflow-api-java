package br.com.petflow.petflow_api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import br.com.petflow.petflow_api.entity.Tutor;
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

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret);
    }

    public String generateToken(Tutor tutor) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(tutor.getEmail())
                .withClaim("id", tutor.getId())
                .withClaim("name", tutor.getName())
                .withClaim("role", tutor.getRole().name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusMillis(expirationMs)))
                .sign(algorithm());
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
}
