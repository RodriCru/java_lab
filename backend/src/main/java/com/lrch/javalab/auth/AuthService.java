package com.lrch.javalab.auth;

import com.lrch.javalab.common.ApiException;
import com.lrch.javalab.mail.MailService;
import com.lrch.javalab.users.User;
import com.lrch.javalab.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public User registrar(RegisterRequest request) {
        String email = normalizar(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_YA_REGISTRADO", "Ese correo ya tiene una cuenta.");
        }
        String nombre = request.displayName() == null || request.displayName().isBlank()
                ? email.substring(0, email.indexOf('@'))
                : request.displayName().trim();

        User user = new User(email, passwordEncoder.encode(request.password()), nombre);
        User guardado = userRepository.save(user);
        // Bienvenida con el correo de acceso, nunca con la contraseña. Si falla, el registro sigue.
        mailService.enviarBienvenida(guardado);
        return guardado;
    }

    @Transactional
    public User iniciarSesion(LoginRequest request) {
        String email = normalizar(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .filter(User::isActive)
                .orElseThrow(() -> ApiException.unauthorized("CREDENCIALES_INVALIDAS",
                        "Correo o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos.");
        }

        user.setLastLoginAt(System.currentTimeMillis());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User actual(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> ApiException.unauthorized("SESION_REQUERIDA", "Tu sesión ya no es válida."));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
