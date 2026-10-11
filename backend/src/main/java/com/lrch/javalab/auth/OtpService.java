package com.lrch.javalab.auth;

import com.lrch.javalab.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Codigos de 6 digitos por correo. Seis digitos se rompen por fuerza bruta en un instante si se
 * guardan con un hash simple, por eso se guarda un HMAC con una clave del servidor, y cada codigo
 * admite pocos intentos, vence pronto y se usa una sola vez.
 */
@Service
public class OtpService {

    static final long VIGENCIA_MS = 10 * 60_000L;
    static final long ESPERA_ENTRE_ENVIOS_MS = 60_000L;
    static final long VENTANA_MS = 60 * 60_000L;
    static final int MAX_ENVIOS_POR_VENTANA = 5;
    static final int MAX_INTENTOS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailCodeRepository repository;
    private final byte[] clave;

    public OtpService(EmailCodeRepository repository, AuthProperties properties) {
        this.repository = repository;
        this.clave = ("otp:" + properties.jwtSecret()).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Genera un codigo nuevo e invalida los anteriores del mismo tipo. Devuelve el codigo en claro.
     * Los limites lanzan antes de escribir nada, asi que {@code noRollbackFor} es seguro y permite
     * que {@link AccountRecoveryService#solicitarRecuperacion} los atrape sin romper su transaccion.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public String emitir(Long userId, CodePurpose proposito, String correoDestino) {
        long ahora = System.currentTimeMillis();

        repository.findFirstByUserIdAndPurposeOrderByCreatedAtDesc(userId, proposito).ifPresent(ultimo -> {
            if (ahora - ultimo.getCreatedAt() < ESPERA_ENTRE_ENVIOS_MS) {
                throw ApiException.tooManyRequests("DEMASIADAS_SOLICITUDES",
                        "Espera un minuto antes de pedir otro código.");
            }
        });
        if (repository.countByUserIdAndPurposeAndCreatedAtGreaterThan(userId, proposito, ahora - VENTANA_MS)
                >= MAX_ENVIOS_POR_VENTANA) {
            throw ApiException.tooManyRequests("DEMASIADAS_SOLICITUDES",
                    "Pediste demasiados códigos. Intenta de nuevo en una hora.");
        }

        for (EmailCode anterior : repository.findByUserIdAndPurposeAndUsedAtIsNull(userId, proposito)) {
            anterior.marcarUsado(ahora);
        }

        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        repository.save(new EmailCode(userId, proposito, firmar(userId, proposito, codigo),
                correoDestino, ahora, ahora + VIGENCIA_MS));
        return codigo;
    }

    /**
     * Comprueba el codigo y, si es correcto, lo consume. El intento fallido se conserva aunque se
     * lance la excepcion ({@code noRollbackFor}); los llamadores deben declararlo tambien.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public EmailCode verificar(Long userId, CodePurpose proposito, String codigo) {
        long ahora = System.currentTimeMillis();
        EmailCode vigente = repository
                .findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(userId, proposito)
                .orElseThrow(() -> codigoInvalido());

        if (vigente.getExpiresAt() < ahora) {
            vigente.marcarUsado(ahora);
            throw ApiException.badRequest("CODIGO_EXPIRADO", "El código venció. Pide uno nuevo.");
        }
        if (vigente.getAttempts() >= MAX_INTENTOS) {
            vigente.marcarUsado(ahora);
            throw ApiException.badRequest("DEMASIADOS_INTENTOS", "Demasiados intentos. Pide un código nuevo.");
        }

        boolean coincide = MessageDigest.isEqual(
                firmar(userId, proposito, codigo).getBytes(StandardCharsets.UTF_8),
                vigente.getCodeHash().getBytes(StandardCharsets.UTF_8));
        if (!coincide) {
            vigente.registrarIntentoFallido();
            throw codigoInvalido();
        }

        vigente.marcarUsado(ahora);
        return vigente;
    }

    private ApiException codigoInvalido() {
        return ApiException.badRequest("CODIGO_INVALIDO", "El código no es correcto.");
    }

    private String firmar(Long userId, CodePurpose proposito, String codigo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(clave, "HmacSHA256"));
            byte[] firma = mac.doFinal((userId + ":" + proposito + ":" + codigo).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(firma);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HmacSHA256 no disponible", ex);
        }
    }
}
