package com.microservicios.perfiles.messaging;

import com.microservicios.perfiles.service.PerfilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PerfilConsumer {

    private static final Logger log = LoggerFactory.getLogger(PerfilConsumer.class);

    private final PerfilService perfilService;

    public PerfilConsumer(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @RabbitListener(queues = "perfiles.queue")
    public void onEmpleadoCreado(Map<String, Object> evento) {
        String empleadoId = (String) evento.get("id");
        log.info("[CONSUMER] Evento empleado.creado recibido en perfiles para id={}", empleadoId);
        perfilService.crearDesdeEvento(evento);
        log.info("[PERFIL] Perfil por defecto creado/verificado para empleadoId={}", empleadoId);
    }
}
