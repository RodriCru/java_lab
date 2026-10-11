package com.lrch.javalab.admin;

import com.lrch.javalab.auth.AuthProperties;
import com.lrch.javalab.users.User;
import com.lrch.javalab.users.UserRepository;
import com.lrch.javalab.users.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Promueve a SUPER_ADMIN la cuenta de JAVALAB_SUPER_ADMIN_EMAIL (si ya existe). No crea cuentas. */
@Component
public class SuperAdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminBootstrap.class);

    private final AuthProperties properties;
    private final UserRepository userRepository;

    public SuperAdminBootstrap(AuthProperties properties, UserRepository userRepository) {
        this.properties = properties;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        String correo = properties.superAdminEmail();
        if (correo == null || correo.isBlank()) {
            return;
        }
        userRepository.findByEmailIgnoreCase(correo.trim().toLowerCase(Locale.ROOT)).ifPresentOrElse(user -> {
            if (!user.isSuperAdmin()) {
                user.setRole(UserRole.SUPER_ADMIN);
                userRepository.save(user);
                log.info("Cuenta {} promovida a SUPER_ADMIN", user.getEmail());
            }
        }, () -> log.warn("JAVALAB_SUPER_ADMIN_EMAIL={} no corresponde a ninguna cuenta existente", correo));
    }
}
