package com.microservicios.perfiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class PerfilesApplication {
    public static void main(String[] args) {
        SpringApplication.run(PerfilesApplication.class, args);
    }
}
