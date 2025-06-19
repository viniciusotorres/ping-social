package com.pingsocial.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.springframework.security.config.Elements.JWT;

@Service
public class JwtTokenService {

    private static final String SECRET_KEY = "aB1cD2eF3gH4iJ5kL6mN7oP8qR9sT0uV";

    private static final String ISSUER = "pingsocial-api";

    public String generateToken(UserDetailsImpl user, String clientIp) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            return com.auth0.jwt.JWT.create()
                    .withIssuer(ISSUER)
                    .withIssuedAt(Date.from(creationDate()))
                    .withExpiresAt(Date.from(expirationDate()))
                    .withSubject(user.getUsername())
                    .withClaim("longitude", user.getLongitude())
                    .withClaim("latitude", user.getLatitude())
                    .withClaim("ip", clientIp)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token", exception);
        }
    }

    public String getSubjectFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            return com.auth0.jwt.JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token inválido ou expirado", e);
        }
    }

    private Instant creationDate() {
        return ZonedDateTime.now(ZoneId.of("America/Recife")).toInstant();
    }

    private Instant expirationDate() {
        return ZonedDateTime.now(ZoneId.of("America/Recife")).plusHours(4).toInstant();
    }
}
