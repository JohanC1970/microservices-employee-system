package com.microservicios.servidor_departamentos.controller;

import com.microservicios.servidor_departamentos.model.Departamento;
import com.microservicios.servidor_departamentos.service.DepartamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @Operation(summary = "Registrar un nuevo departamento",
            description = "Registra un departamento en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Departamento creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Departamento.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o departamento ya existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Departamento departamento) {
        Departamento nuevo = departamentoService.registrar(departamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @Operation(summary = "Obtener un departamento por ID",
            description = "Devuelve la información de un departamento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Departamento encontrado",
                    content = @Content(schema = @Schema(implementation = Departamento.class))),
            @ApiResponse(responseCode = "404", description = "Departamento no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Departamento> obtener(@PathVariable String id) {
        Departamento departamento = departamentoService.obtener(id);
        return ResponseEntity.ok(departamento);
    }

    @Operation(summary = "Listar todos los departamentos",
            description = "Devuelve la lista completa de departamentos registrados")
    @ApiResponse(responseCode = "200", description = "Lista de departamentos obtenida correctamente")
    @GetMapping
    public List<Departamento> listar() {
        return departamentoService.obtenerTodos();
    }

}
