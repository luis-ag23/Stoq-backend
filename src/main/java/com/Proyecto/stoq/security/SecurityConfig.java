package com.Proyecto.stoq.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .requestMatchers("/api/auth/**").permitAll()

                .requestMatchers("/api/usuarios/me").authenticated()
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET,
                        "/api/productos/**",
                        "/api/categorias/**",
                        "/api/unidades/**"
                ).authenticated()

                .requestMatchers(HttpMethod.POST, "/api/productos/**")
                    .hasAnyRole("ADMIN", "OPERADOR","GERENTE")

                .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                    .hasAnyRole("ADMIN", "OPERADOR","GERENTE")

                .requestMatchers(HttpMethod.DELETE, "/api/productos/**")
                    .hasRole("GERENTE")

                .requestMatchers(HttpMethod.POST,
                        "/api/categorias/**",
                        "/api/unidades/**"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.PUT,
                        "/api/categorias/**",
                        "/api/unidades/**"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.DELETE,
                        "/api/categorias/**",
                        "/api/unidades/**"
                ).hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/roles/**").permitAll()

                .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/movimientos/**")
                    .hasAnyRole("ADMIN", "GERENTE")

                .requestMatchers(HttpMethod.POST, "/api/movimientos/**")
                    .hasAnyRole("ADMIN", "OPERADOR", "GERENTE")

                .requestMatchers(HttpMethod.GET, "/api/reportes/**")
                    .hasAnyRole("ADMIN", "GERENTE")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "https://*.netlify.app"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}