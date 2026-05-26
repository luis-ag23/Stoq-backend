package com.Proyecto.stoq.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final byte[] secretBytes;

    public JwtService(@Value("${stoq.security.jwt.secret}") String secret) {
        String resolvedSecret = secret != null ? secret.trim() : "";

        if (resolvedSecret.isBlank()) {
            throw new IllegalStateException("JWT secret no configurado");
        }

        this.secretBytes = resolvedSecret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(String email){

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(secretBytes))
                .compact();
    }

    public String extractEmail(String token){
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token){

        try{
            extractClaims(token);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    private Claims extractClaims(String token){

        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretBytes))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
