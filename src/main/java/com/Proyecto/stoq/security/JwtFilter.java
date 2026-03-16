package com.Proyecto.stoq.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.Proyecto.stoq.domain.model.Usuario;
import com.Proyecto.stoq.infrastructure.persistence.repositories.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtFilter(JwtService jwtService,
                     UsuarioRepository usuarioRepository){
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = authHeader.substring(7);

        if(!jwtService.isTokenValid(token)){
            filterChain.doFilter(request,response);
            return;
        }

        String email = jwtService.extractEmail(token);

        Usuario usuario = usuarioRepository
                .findByCorreo(email)
                .orElse(null);


        System.out.println("TOKEN: " + token);
        System.out.println("EMAIL EXTRAIDO: " + email);
        System.out.println("USUARIO EN BD: " + usuario);

        if(usuario != null){

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(auth);
        }

        filterChain.doFilter(request,response);
    }
}
