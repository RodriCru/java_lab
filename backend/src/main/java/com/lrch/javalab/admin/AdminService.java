package com.lrch.javalab.admin;

import com.lrch.javalab.auth.PasswordGenerator;
import com.lrch.javalab.common.ApiException;
import com.lrch.javalab.mail.MailService;
import com.lrch.javalab.users.User;
import com.lrch.javalab.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Administracion minima: invitar usuarios y activarlos o desactivarlos. El rol se comprueba contra la
 * base de datos en cada llamada, no contra el token: quitar el rol surte efecto de inmediato.
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public List<User> listar(Long adminId) {
        exigirSuperAdmin(adminId);
        return userRepository.findAllByOrderByIdDesc();
    }

    /** Crea la cuenta con una contrasena temporal aleatoria y se la envia por correo. */
    @Transactional
    public User invitar(Long adminId, InviteRequest request) {
        exigirSuperAdmin(adminId);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_YA_REGISTRADO", "Ese correo ya tiene una cuenta.");
        }
        String nombre = request.displayName() == null || request.displayName().isBlank()
                ? email.substring(0, email.indexOf('@'))
                : request.displayName().trim();

        String temporal = PasswordGenerator.generar();
        User user = userRepository.save(new User(email, passwordEncoder.encode(temporal), nombre));
        mailService.enviarInvitacion(user, temporal);
        return user;
    }

    /** Genera otra contrasena temporal (la anterior deja de servir) y la reenvia. */
    @Transactional
    public User reenviarInvitacion(Long adminId, Long userId) {
        exigirSuperAdmin(adminId);
        User objetivo = usuario(userId);
        if (objetivo.isSuperAdmin()) {
            throw ApiException.badRequest("OPERACION_NO_PERMITIDA",
                    "A un super administrador no se le restablece la contraseña desde aquí.");
        }
        if (!objetivo.isActive()) {
            throw ApiException.badRequest("USUARIO_INACTIVO", "La cuenta está desactivada.");
        }
        String temporal = PasswordGenerator.generar();
        objetivo.setPasswordHash(passwordEncoder.encode(temporal));
        userRepository.save(objetivo);
        mailService.enviarInvitacion(objetivo, temporal);
        return objetivo;
    }

    @Transactional
    public User cambiarEstado(Long adminId, Long userId, boolean activo) {
        exigirSuperAdmin(adminId);
        User objetivo = usuario(userId);
        if (objetivo.isSuperAdmin()) {
            throw ApiException.badRequest("OPERACION_NO_PERMITIDA",
                    "No se puede desactivar a un super administrador.");
        }
        if (activo) {
            objetivo.activate();
        } else {
            objetivo.deactivate();
        }
        return userRepository.save(objetivo);
    }

    private void exigirSuperAdmin(Long userId) {
        boolean esAdmin = userRepository.findById(userId)
                .filter(User::isActive)
                .map(User::isSuperAdmin)
                .orElse(false);
        if (!esAdmin) {
            throw ApiException.forbidden("ACCESO_DENEGADO", "No tienes permisos para esta acción.");
        }
    }

    private User usuario(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USUARIO_NO_ENCONTRADO", "Ese usuario no existe."));
    }
}
