package com.microservicios.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;
    private final long recoveryExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs,
            @Value("${jwt.recovery-expiration-ms:3600000}") long recoveryExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.recoveryExpirationMs = recoveryExpirationMs;
    }

    /** Genera el JWT de acceso con sub=email y claim rol */
    public String generarToken(String email, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("role", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Genera un JWT de corta duración para recuperación/establecimiento de contraseña */
    public String generarTokenRecuperacion(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("tipo", "RECUPERACION")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + recoveryExpirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parsearToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerEmail(String token) {
        return parsearToken(token).getSubject();
    }

    public boolean esValido(String token) {
        try {
            parsearToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
