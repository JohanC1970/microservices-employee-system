package com.microservicios.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Solicitud de recuperación de contraseña")
public class RecoverPasswordRequest {
    @Schema(description = "Email del usuario", example = "usuario@empresa.com")
    private String email;
}
