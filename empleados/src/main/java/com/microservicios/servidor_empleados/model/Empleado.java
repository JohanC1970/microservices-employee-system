package com.microservicios.servidor_empleados.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Schema(description = "Entidad que representa un empleado dentro del sistema")
public class Empleado {

    @Id
    @Schema(description = "Identificador único del empleado")
    private String id;

    @Schema(description = "Nombre completo del empleado")
    private String nombre;

    @Schema(description = "Correo electrónico del empleado")
    private String email;

    @Schema(description = "Identificador del departamento al que pertenece")
    private String departamentoId;

    @Schema(description = "Fecha de ingreso del empleado")
    private LocalDate fechaIngreso;

}