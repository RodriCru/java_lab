package com.lrch.javalab.admin;

import com.lrch.javalab.auth.AuthenticatedUser;
import com.lrch.javalab.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserResponse>> usuarios(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.ok(adminService.listar(principal.id()).stream().map(AdminUserResponse::from).toList());
    }

    @PostMapping("/invitations")
    public ResponseEntity<ApiResponse<AdminUserResponse>> invitar(@AuthenticationPrincipal AuthenticatedUser principal,
                                                                  @Valid @RequestBody InviteRequest request) {
        AdminUserResponse creado = AdminUserResponse.from(adminService.invitar(principal.id(), request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(creado, "Invitación enviada a " + creado.email() + "."));
    }

    @PostMapping("/users/{id}/resend-invitation")
    public ApiResponse<AdminUserResponse> reenviar(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @PathVariable Long id) {
        AdminUserResponse usuario = AdminUserResponse.from(adminService.reenviarInvitacion(principal.id(), id));
        return ApiResponse.ok(usuario, "Se generó otra contraseña temporal y se envió a " + usuario.email() + ".");
    }

    @PostMapping("/users/{id}/active")
    public ApiResponse<AdminUserResponse> cambiarEstado(@AuthenticationPrincipal AuthenticatedUser principal,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody SetActiveRequest request) {
        AdminUserResponse usuario = AdminUserResponse.from(
                adminService.cambiarEstado(principal.id(), id, request.active()));
        return ApiResponse.ok(usuario, usuario.active() ? "Cuenta activada." : "Cuenta desactivada.");
    }
}
