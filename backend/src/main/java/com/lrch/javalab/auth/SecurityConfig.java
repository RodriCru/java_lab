package com.lrch.javalab.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lrch.javalab.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthProperties authProperties,
                          ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authProperties = authProperties;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // La sesion vive en una cookie JWT con SameSite=Lax, lo que bloquea el envio
                // de la cookie en peticiones cross-site. Por eso no se usa el token CSRF.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.deny()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login",
                                "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/health", "/api/v1/auth/registration-status").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) ->
                                escribirError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "SESION_REQUERIDA", "Necesitas iniciar sesión para continuar."))
                        .accessDeniedHandler((request, response, ex) ->
                                escribirError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "ACCESO_DENEGADO", "No tienes permisos para esta acción.")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void escribirError(HttpServletResponse response, int status, String code, String message)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(code, message));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origenes = authProperties.originList();

        // Con credenciales (la cookie de sesion), Spring prohibe "Access-Control-Allow-Origin: *".
        // Un comodin como "https://*.midominio.com" o "*" solo es valido en allowedOriginPatterns.
        // Asi, una mala configuracion no tumba cada peticion con un 500: se degrada a patrones.
        if (origenes.stream().anyMatch(origen -> origen.contains("*"))) {
            log.warn("JAVALAB_ALLOWED_ORIGINS contiene un comodin ({}): se usan patrones de origen. "
                    + "En produccion conviene listar los dominios exactos.", origenes);
            config.setAllowedOriginPatterns(origenes);
        } else {
            config.setAllowedOrigins(origenes);
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Accept", "Authorization"));
        // Necesario para que el navegador envie y acepte la cookie de sesion.
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
