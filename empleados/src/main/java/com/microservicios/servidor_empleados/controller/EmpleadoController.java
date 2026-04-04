package com.microservicios.servidor_empleados.controller;

import com.microservicios.servidor_empleados.model.Empleado;
import com.microservicios.servidor_empleados.service.EmpleadoService;
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
@RequestMapping("/empleados")
public class EmpleadoController {

        private final EmpleadoService empleadoService;

        public EmpleadoController(EmpleadoService empleadoService) {
                this.empleadoService = empleadoService;
        }

        @Operation(summary = "Registrar un nuevo empleado", description = "Registra un empleado validando previamente que el departamento exista. Publica evento empleado.creado")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Empleado creado exitosamente", content = @Content(schema = @Schema(implementation = Empleado.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos o departamento inexistente"),
                        @ApiResponse(responseCode = "503", description = "Servicio de departamentos no disponible"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @PostMapping
        public ResponseEntity<?> registrar(@RequestBody Empleado empleado) {
                Empleado nuevo = empleadoService.registrar(empleado);
                return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        }

        @Operation(summary = "Obtener un empleado por ID", description = "Devuelve la información de un empleado específico")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Empleado encontrado", content = @Content(schema = @Schema(implementation = Empleado.class))),
                        @ApiResponse(responseCode = "400", description = "Empleado no encontrado"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @GetMapping("/{id}")
        public ResponseEntity<Empleado> obtener(@PathVariable String id) {
                Empleado empleado = empleadoService.obtener(id);
                return ResponseEntity.ok(empleado);
        }

        @Operation(summary = "Listar todos los empleados", description = "Devuelve la lista completa de empleados registrados")
        @ApiResponse(responseCode = "200", description = "Lista de empleados obtenida correctamente")
        @GetMapping
        public List<Empleado> listar() {
                return empleadoService.obtenerTodos();
        }

        @Operation(summary = "Eliminar un empleado", description = "Elimina el empleado de la base de datos y publica el evento empleado.eliminado")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Empleado eliminado exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Empleado no encontrado"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminar(@PathVariable String id) {
                empleadoService.eliminar(id);
                return ResponseEntity.noContent().build();
        }
}
