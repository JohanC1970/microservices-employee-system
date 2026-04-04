package com.microservicios.notificaciones.messaging;

import com.microservicios.notificaciones.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConsumer.class);

    private final NotificacionService notificacionService;

    public NotificacionConsumer(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @RabbitListener(queues = "notificaciones.queue")
    public void onMensaje(Map<String, Object> evento) {
        String routingKey = "";
        // Determinamos tipo por campos presentes
        if (evento.containsKey("departamentoId")) {
            onEmpleadoCreado(evento);
        } else {
            onEmpleadoEliminado(evento);
        }
    }

    public void onEmpleadoCreado(Map<String, Object> evento) {
        String empleadoId = (String) evento.get("id");
        String nombre = (String) evento.get("nombre");
        String email = (String) evento.get("email");
        log.info("[CONSUMER] Evento empleado.creado recibido para id={}", empleadoId);
        notificacionService.registrarBienvenida(empleadoId, nombre, email);
    }

    public void onEmpleadoEliminado(Map<String, Object> evento) {
        String empleadoId = (String) evento.get("id");
        String nombre = (String) evento.get("nombre");
        String email = (String) evento.get("email");
        log.info("[CONSUMER] Evento empleado.eliminado recibido para id={}", empleadoId);
        notificacionService.registrarDesvinculacion(empleadoId, nombre, email);
    }
}
