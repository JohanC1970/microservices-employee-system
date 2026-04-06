package com.microservicios.servidor_empleados.event;

import com.microservicios.servidor_empleados.config.RabbitMQConfig;
import com.microservicios.servidor_empleados.model.Empleado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmpleadoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EmpleadoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EmpleadoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEmpleadoCreado(Empleado empleado) {
        try {
            Map<String, Object> evento = new HashMap<>();
            evento.put("id",            empleado.getId());
            evento.put("nombre",        empleado.getNombre());
            evento.put("email",         empleado.getEmail());
            evento.put("departamentoId",empleado.getDepartamentoId());
            evento.put("fechaIngreso",  empleado.getFechaIngreso() != null
                    ? empleado.getFechaIngreso().toString() : null);
            evento.put("tipo", "CREADO");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_CREADO,
                    evento);

            log.info("[EVENTO] empleado.creado publicado para id={}", empleado.getId());
        } catch (Exception ex) {
            log.error("[EVENTO] Fallo al publicar empleado.creado para id={}: {}",
                    empleado.getId(), ex.getMessage());
        }
    }

    public void publicarEmpleadoEliminado(Empleado empleado) {
        try {
            Map<String, Object> evento = new HashMap<>();
            evento.put("id",     empleado.getId());
            evento.put("nombre", empleado.getNombre());
            evento.put("email",  empleado.getEmail());
            evento.put("tipo",   "ELIMINADO");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_ELIMINADO,
                    evento);

            log.info("[EVENTO] empleado.eliminado publicado para id={}", empleado.getId());
        } catch (Exception ex) {
            log.error("[EVENTO] Fallo al publicar empleado.eliminado para id={}: {}",
                    empleado.getId(), ex.getMessage());
        }
    }
}
