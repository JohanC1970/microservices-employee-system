package com.microservicios.servidor_departamentos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Schema(description = "Entidad que representa un departamento dentro del sistema")
public class Departamento {

    @Id
    @Schema(description = "Identificador único del departamento")
    private String id;

    @Schema(description = "Nombre del departamento")
    private String nombre;

    @Schema(description = "Descripción del departamento")
    private String descripcion;

}
