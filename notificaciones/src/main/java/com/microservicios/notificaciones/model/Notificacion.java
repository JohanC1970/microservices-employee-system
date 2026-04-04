package com.microservicios.notificaciones.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notificaciones")
@Schema(description = "Registro de una notificación enviada")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identificador único de la notificación")
    private String id;

    @Schema(description = "Tipo: BIENVENIDA o DESVINCULACION")
    private String tipo;

    @Schema(description = "Email del destinatario")
    private String destinatario;

    @Schema(description = "Contenido del mensaje")
    @Column(length = 500)
    private String mensaje;

    @Schema(description = "Fecha y hora de envío")
    private LocalDateTime fechaEnvio;

    @Schema(description = "ID del empleado asociado")
    private String empleadoId;
}
