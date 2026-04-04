package com.microservicios.perfiles.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfiles")
@Schema(description = "Perfil de un empleado")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identificador único del perfil")
    private String id;

    @Column(unique = true, nullable = false)
    @Schema(description = "ID del empleado asociado")
    private String empleadoId;

    @Schema(description = "Nombre del empleado")
    private String nombre;

    @Schema(description = "Email del empleado")
    private String email;

    @Schema(description = "Número de teléfono")
    private String telefono = "";

    @Schema(description = "Dirección")
    private String direccion = "";

    @Schema(description = "Ciudad")
    private String ciudad = "";

    @Schema(description = "Biografía breve")
    @Column(length = 1000)
    private String biografia = "";

    @Schema(description = "Fecha de creación del perfil")
    private LocalDateTime fechaCreacion;
}
