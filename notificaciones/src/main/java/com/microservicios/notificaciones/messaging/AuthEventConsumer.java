package com.microservicios.notificaciones.messaging;

import com.microservicios.notificaciones.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthEventConsumer.class);

    private final NotificacionService notificacionService;

    public AuthEventConsumer(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @RabbitListener(queues = "notificaciones.usuario.creado.queue")
    public void onUsuarioCreado(Map<String, Object> evento) {
        String email = (String) evento.get("email");
        String nombre = (String) evento.get("nombre");
        String token = (String) evento.get("token");
        log.info("[NOTIF-CONSUMER] usuario.creado recibido para: {}", email);
        notificacionService.registrarEstablecimientoPassword(email, nombre, token);
    }

    @RabbitListener(queues = "notificaciones.usuario.recuperacion.queue")
    public void onUsuarioRecuperacion(Map<String, Object> evento) {
        String email = (String) evento.get("email");
        String token = (String) evento.get("token");
        log.info("[NOTIF-CONSUMER] usuario.recuperacion recibido para: {}", email);
        notificacionService.registrarRecuperacionPassword(email, token);
    }
}
