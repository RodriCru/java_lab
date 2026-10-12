package com.lrch.javalab.auth;

import com.lrch.javalab.users.User;
import com.lrch.javalab.users.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Lee el JWT de la cookie httpOnly y, si es valido, carga el usuario y lo deja en el
 * SecurityContext. Si el token no es valido la peticion sigue sin autenticar.
 *
 * <p>Cuando un token presente no sirve, se registra el motivo (caducado, usuario inexistente
 * o cuenta inactiva): sin ese log, un 401 en el frontend es indistinguible de no llevar sesion.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthProperties properties;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, AuthProperties properties) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        leerToken(request).ifPresent(token -> autenticarSiProcede(token, request));

        chain.doFilter(request, response);
    }

    /** Un token presente que no autentica deja un rastro claro en el log. */
    private void autenticarSiProcede(String token, HttpServletRequest request) {
        Optional<Long> userId = jwtService.extractUserId(token);
        if (userId.isEmpty()) {
            return;
        }

        Optional<User> encontrado = userRepository.findById(userId.get());
        if (encontrado.isEmpty()) {
            log.warn("El token apunta al usuario {} pero no existe en la base de datos "
                    + "(¿base de datos distinta de la que emitio el token?)", userId.get());
            return;
        }

        User user = encontrado.get();
        if (!user.isActive()) {
            log.info("La cuenta {} ({}) esta inactiva: se rechaza la peticion", user.getId(), user.getEmail());
            return;
        }

        autenticar(user, request);
    }

    private void autenticar(User user, HttpServletRequest request) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(), user.getDisplayName());
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Optional<String> leerToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(c -> properties.cookieName().equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();
    }
}
