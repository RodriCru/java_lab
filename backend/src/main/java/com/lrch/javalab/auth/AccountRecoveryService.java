package com.lrch.javalab.auth;

import com.lrch.javalab.common.ApiException;
import com.lrch.javalab.mail.MailService;
import com.lrch.javalab.users.User;
import com.lrch.javalab.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Recuperacion de contrasena y cambio de correo, ambos con codigo OTP enviado por correo.
 *
 * <p>Los metodos que verifican un codigo declaran {@code noRollbackFor}: el contador de intentos
 * fallidos debe guardarse aunque se lance la excepcion.
 */
@Service
public class AccountRecoveryService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final MailService mailService;

    public AccountRecoveryService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                  OtpService otpService, MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.mailService = mailService;
    }

    /**
     * Envia el codigo si la cuenta existe. Nunca revela si existe: ni con errores ni con limites de
     * frecuencia, el llamador siempre responde lo mismo.
     */
    @Transactional
    public void solicitarRecuperacion(String correo) {
        userRepository.findByEmailIgnoreCase(normalizar(correo)).filter(User::isActive).ifPresent(user -> {
            try {
                String codigo = otpService.emitir(user.getId(), CodePurpose.PASSWORD_RESET, user.getEmail());
                mailService.enviarCodigo(user.getEmail(), user.getDisplayName(), CodePurpose.PASSWORD_RESET, codigo);
            } catch (ApiException limite) {
                // Limite de frecuencia: se calla a proposito para no delatar que la cuenta existe.
            }
        });
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void restablecerContrasena(ResetPasswordRequest request) {
        // Correo inexistente y codigo incorrecto responden igual.
        User user = userRepository.findByEmailIgnoreCase(normalizar(request.email()))
                .filter(User::isActive)
                .orElseThrow(() -> ApiException.badRequest("CODIGO_INVALIDO", "El código no es correcto."));

        otpService.verificar(user.getId(), CodePurpose.PASSWORD_RESET, request.code());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    /** Paso 1 del cambio de correo: el codigo viaja al correo NUEVO para comprobar que es del usuario. */
    @Transactional
    public void solicitarCambioDeCorreo(Long userId, ChangeEmailRequest request) {
        User user = usuarioActivo(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("CONTRASENA_INCORRECTA", "La contraseña actual no es correcta.");
        }
        String nuevo = normalizar(request.newEmail());
        if (nuevo.equals(user.getEmail())) {
            throw ApiException.badRequest("CORREO_IGUAL", "Ese ya es el correo de tu cuenta.");
        }
        if (userRepository.existsByEmailIgnoreCase(nuevo)) {
            throw ApiException.conflict("EMAIL_YA_REGISTRADO", "Ese correo ya tiene una cuenta.");
        }

        String codigo = otpService.emitir(user.getId(), CodePurpose.EMAIL_CHANGE, nuevo);
        mailService.enviarCodigo(nuevo, user.getDisplayName(), CodePurpose.EMAIL_CHANGE, codigo);
    }

    /** Paso 2: con el codigo correcto el correo cambia. Avisa al correo anterior. */
    @Transactional(noRollbackFor = ApiException.class)
    public User confirmarCambioDeCorreo(Long userId, String codigo) {
        User user = usuarioActivo(userId);
        EmailCode registro = otpService.verificar(userId, CodePurpose.EMAIL_CHANGE, codigo);

        String nuevo = registro.getTargetEmail();
        if (userRepository.existsByEmailIgnoreCase(nuevo)) {
            throw ApiException.conflict("EMAIL_YA_REGISTRADO", "Ese correo ya tiene una cuenta.");
        }
        String anterior = user.getEmail();
        user.setEmail(nuevo);
        userRepository.save(user);
        mailService.enviarAvisoCambioCorreo(anterior, user.getDisplayName(), nuevo);
        return user;
    }

    private User usuarioActivo(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> ApiException.unauthorized("SESION_REQUERIDA", "Tu sesión ya no es válida."));
    }

    private String normalizar(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
