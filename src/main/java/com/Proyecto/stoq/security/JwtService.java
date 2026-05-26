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

    private final byte[] secretKey;

    public JwtService(@Value("${STOQ_JWT_SECRET}") String secret) {
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalStateException("STOQ_JWT_SECRET must be at least 32 characters long");
        }
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(String email){

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(Keys.hmacShaKeyFor(secretKey))
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
            .verifyWith(Keys.hmacShaKeyFor(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
