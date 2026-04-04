package com.microservicios.notificaciones.controller;

import com.microservicios.notificaciones.model.Notificacion;
import com.microservicios.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@Tag(name = "Notificaciones", description = "Consulta del historial de notificaciones enviadas")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Operation(summary = "Listar todas las notificaciones")
    @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida correctamente")
    @GetMapping
    public ResponseEntity<List<Notificacion>> listar() {
        return ResponseEntity.ok(notificacionService.obtenerTodas());
    }

    @Operation(summary = "Listar notificaciones de un empleado")
    @ApiResponse(responseCode = "200", description = "Notificaciones del empleado encontradas")
    @GetMapping("/{empleadoId}")
    public ResponseEntity<List<Notificacion>> listarPorEmpleado(@PathVariable String empleadoId) {
        return ResponseEntity.ok(notificacionService.obtenerPorEmpleado(empleadoId));
    }
}
