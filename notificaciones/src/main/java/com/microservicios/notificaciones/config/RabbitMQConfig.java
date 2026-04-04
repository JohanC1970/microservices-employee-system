package com.microservicios.notificaciones.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ── Cola existente de empleados ──
    @Bean
    public Queue notificacionesQueue() {
        return new Queue("notificaciones.queue", true);
    }

    // ── Exchange de auth (publicado por auth-service) ──
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange("auth.exchange", true, false);
    }

    @Bean
    public Queue usuarioCreadoQueue() {
        return new Queue("notificaciones.usuario.creado.queue", true);
    }

    @Bean
    public Queue usuarioRecuperacionQueue() {
        return new Queue("notificaciones.usuario.recuperacion.queue", true);
    }

    @Bean
    public Binding bindingUsuarioCreado() {
        return BindingBuilder.bind(usuarioCreadoQueue())
                .to(authExchange())
                .with("usuario.creado");
    }

    @Bean
    public Binding bindingUsuarioRecuperacion() {
        return BindingBuilder.bind(usuarioRecuperacionQueue())
                .to(authExchange())
                .with("usuario.recuperacion");
    }
}
