package com.microservicios.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Credenciales de acceso")
public class LoginRequest {
    @Schema(description = "Email del usuario", example = "admin@empresa.com")
    private String email;
    @Schema(description = "Contraseña", example = "secret123")
    private String password;
}
