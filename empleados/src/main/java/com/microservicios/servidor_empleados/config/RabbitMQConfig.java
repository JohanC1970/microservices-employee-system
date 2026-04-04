package com.microservicios.servidor_empleados.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE       = "empleados.exchange";
    public static final String QUEUE_NOTIF    = "notificaciones.queue";
    public static final String QUEUE_PERFILES = "perfiles.queue";
    public static final String ROUTING_CREADO    = "empleado.creado";
    public static final String ROUTING_ELIMINADO = "empleado.eliminado";

    @Bean
    public TopicExchange empleadosExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    /* ── Colas ─────────────────────────────────────────────── */
    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(QUEUE_NOTIF).build();
    }

    @Bean
    public Queue perfilesQueue() {
        return QueueBuilder.durable(QUEUE_PERFILES).build();
    }

    /* ── Bindings ──────────────────────────────────────────── */
    // notificaciones escucha empleado.creado  Y  empleado.eliminado
    @Bean
    public Binding bindNotifCreado() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(empleadosExchange()).with(ROUTING_CREADO);
    }

    @Bean
    public Binding bindNotifEliminado() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(empleadosExchange()).with(ROUTING_ELIMINADO);
    }

    // perfiles solo escucha empleado.creado
    @Bean
    public Binding bindPerfilesCreado() {
        return BindingBuilder.bind(perfilesQueue())
                .to(empleadosExchange()).with(ROUTING_CREADO);
    }

    /* ── Converter JSON ────────────────────────────────────── */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
