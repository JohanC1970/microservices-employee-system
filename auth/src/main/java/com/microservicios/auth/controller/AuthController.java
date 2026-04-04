package com.microservicios.auth.controller;

import com.microservicios.auth.dto.LoginRequest;
import com.microservicios.auth.dto.RecoverPasswordRequest;
import com.microservicios.auth.dto.ResetPasswordRequest;
import com.microservicios.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints de login y recuperación de contraseña")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Retorna un JWT de acceso")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/recover-password")
    @Operation(summary = "Recuperar contraseña", description = "Envía email con token de recuperación")
    public ResponseEntity<Map<String, String>> recoverPassword(@RequestBody RecoverPasswordRequest request) {
        authService.recoverPassword(request);
        return ResponseEntity.ok(Map.of("mensaje", "Se ha enviado un email con instrucciones"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", description = "Procesa el token y actualiza la contraseña")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
    }
}
