package com.microservicios.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Solicitud de restablecimiento de contraseña")
public class ResetPasswordRequest {
    @Schema(description = "Token de recuperación recibido por email")
    private String token;
    @Schema(description = "Nueva contraseña", example = "nuevaPass123")
    private String nuevaPassword;
}
