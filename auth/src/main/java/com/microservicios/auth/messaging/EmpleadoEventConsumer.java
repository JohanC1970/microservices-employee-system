package com.microservicios.auth.messaging;

import com.microservicios.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmpleadoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmpleadoEventConsumer.class);

    private final AuthService authService;

    public EmpleadoEventConsumer(AuthService authService) {
        this.authService = authService;
    }

    @RabbitListener(queues = "auth.empleado.creado.queue")
    public void onEmpleadoCreado(Map<String, Object> evento) {
        String empleadoId = (String) evento.get("id");
        String email = (String) evento.get("email");
        String nombre = (String) evento.get("nombre");
        log.info("[AUTH-CONSUMER] empleado.creado recibido: id={}, email={}", empleadoId, email);
        authService.crearUsuarioDesdeEmpleado(empleadoId, email, nombre);
    }

    @RabbitListener(queues = "auth.empleado.eliminado.queue")
    public void onEmpleadoEliminado(Map<String, Object> evento) {
        String empleadoId = (String) evento.get("id");
        log.info("[AUTH-CONSUMER] empleado.eliminado recibido: id={}", empleadoId);
        authService.desactivarUsuario(empleadoId);
    }
}
