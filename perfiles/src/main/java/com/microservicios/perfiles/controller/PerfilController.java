package com.microservicios.perfiles.controller;

import com.microservicios.perfiles.model.Perfil;
import com.microservicios.perfiles.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/perfiles")
@Tag(name = "Perfiles", description = "Gestión de perfiles de empleados")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @Operation(summary = "Listar todos los perfiles")
    @ApiResponse(responseCode = "200", description = "Lista de perfiles obtenida correctamente")
    @GetMapping
    public ResponseEntity<List<Perfil>> listar() {
        return ResponseEntity.ok(perfilService.obtenerTodos());
    }

    @Operation(summary = "Obtener perfil por ID de empleado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    })
    @GetMapping("/{empleadoId}")
    public ResponseEntity<Perfil> obtener(@PathVariable String empleadoId) {
        Perfil perfil = perfilService.obtenerPorEmpleado(empleadoId);
        return ResponseEntity.ok(perfil);
    }

    @Operation(summary = "Actualizar perfil de un empleado", description = "Actualiza campos opcionales: teléfono, dirección, ciudad, biografía")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    })
    @PutMapping("/{empleadoId}")
    public ResponseEntity<Perfil> actualizar(@PathVariable String empleadoId,
            @RequestBody Perfil datos) {
        Perfil actualizado = perfilService.actualizar(empleadoId, datos);
        return ResponseEntity.ok(actualizado);
    }
}
