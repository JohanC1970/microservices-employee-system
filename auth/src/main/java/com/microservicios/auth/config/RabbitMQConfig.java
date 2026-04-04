package com.microservicios.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Exchange de empleados (publicado por empleados-service) ──
    public static final String EMPLEADOS_EXCHANGE = "empleados.exchange";
    public static final String QUEUE_EMPLEADO_CREADO = "auth.empleado.creado.queue";
    public static final String QUEUE_EMPLEADO_ELIMINADO = "auth.empleado.eliminado.queue";

    // ── Exchange de auth (publicado por este servicio) ──
    public static final String AUTH_EXCHANGE = "auth.exchange";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Exchange de empleados (debe existir, lo declara empleados-service también)
    @Bean
    public TopicExchange empleadosExchange() {
        return new TopicExchange(EMPLEADOS_EXCHANGE, true, false);
    }

    // Exchange de auth para publicar usuario.creado y usuario.recuperacion
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE, true, false);
    }

    @Bean
    public Queue authEmpleadoCreadoQueue() {
        return new Queue(QUEUE_EMPLEADO_CREADO, true);
    }

    @Bean
    public Queue authEmpleadoEliminadoQueue() {
        return new Queue(QUEUE_EMPLEADO_ELIMINADO, true);
    }

    @Bean
    public Binding bindingEmpleadoCreado() {
        return BindingBuilder.bind(authEmpleadoCreadoQueue())
                .to(empleadosExchange())
                .with("empleado.creado");
    }

    @Bean
    public Binding bindingEmpleadoEliminado() {
        return BindingBuilder.bind(authEmpleadoEliminadoQueue())
                .to(empleadosExchange())
                .with("empleado.eliminado");
    }
}
