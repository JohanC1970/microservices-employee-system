package com.microservicios.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class NotificacionesApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificacionesApplication.class, args);
    }
}
