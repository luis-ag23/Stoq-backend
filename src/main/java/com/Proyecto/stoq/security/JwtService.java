package com.Proyecto.stoq.security;

import java.util.Date;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String SECRET =
            "mi_clave_super_secreta_muy_larga_para_jwt_123456";

    public String generateToken(String email){

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
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
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
