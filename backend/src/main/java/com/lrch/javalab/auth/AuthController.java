package com.lrch.javalab.auth;

import com.lrch.javalab.common.ApiResponse;
import com.lrch.javalab.users.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AccountRecoveryService recoveryService;
    private final JwtService jwtService;
    private final AuthProperties properties;

    public AuthController(AuthService authService, AccountRecoveryService recoveryService,
                          JwtService jwtService, AuthProperties properties) {
        this.authService = authService;
        this.recoveryService = recoveryService;
        this.jwtService = jwtService;
        this.properties = properties;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registrar(@Valid @RequestBody RegisterRequest request) {
        User user = authService.registrar(request);
        return respuestaConCookie(user, HttpStatus.CREATED, "Cuenta creada. Tu sesión ya está activa.");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        User user = authService.iniciarSesion(request);
        return respuestaConCookie(user, HttpStatus.OK, "Sesión iniciada.");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> cerrarSesion() {
        ResponseCookie cookie = ResponseCookie.from(properties.cookieName(), "")
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.ok(null, "Sesión cerrada."));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> actual(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = authService.actual(principal.id());
        return ApiResponse.ok(UserResponse.from(user));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> olvideContrasena(@Valid @RequestBody ForgotPasswordRequest request) {
        recoveryService.solicitarRecuperacion(request.email());
        // Misma respuesta exista o no la cuenta.
        return ApiResponse.ok(null, "Si el correo tiene una cuenta, te enviamos un código de 6 dígitos.");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> restablecerContrasena(@Valid @RequestBody ResetPasswordRequest request) {
        recoveryService.restablecerContrasena(request);
        return ApiResponse.ok(null, "Contraseña actualizada. Ya puedes iniciar sesión.");
    }

    @PostMapping("/me/email")
    public ApiResponse<Void> pedirCambioDeCorreo(@AuthenticationPrincipal AuthenticatedUser principal,
                                                 @Valid @RequestBody ChangeEmailRequest request) {
        recoveryService.solicitarCambioDeCorreo(principal.id(), request);
        return ApiResponse.ok(null, "Te enviamos un código de 6 dígitos al correo nuevo.");
    }

    @PostMapping("/me/email/confirm")
    public ResponseEntity<ApiResponse<UserResponse>> confirmarCambioDeCorreo(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ConfirmEmailRequest request) {
        User user = recoveryService.confirmarCambioDeCorreo(principal.id(), request.code());
        // El JWT lleva el correo como claim: se vuelve a emitir con el correo nuevo.
        return respuestaConCookie(user, HttpStatus.OK, "Correo actualizado.");
    }

    private ResponseEntity<ApiResponse<UserResponse>> respuestaConCookie(User user, HttpStatus status, String mensaje) {
        ResponseCookie cookie = ResponseCookie.from(properties.cookieName(),
                        jwtService.generateToken(user.getId(), user.getEmail()))
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(jwtService.expirationMillis()))
                .build();

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.ok(UserResponse.from(user), mensaje));
    }
}
