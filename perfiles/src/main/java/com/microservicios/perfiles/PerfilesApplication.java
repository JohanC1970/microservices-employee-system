package com.microservicios.perfiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PerfilesApplication {
    public static void main(String[] args) {
        SpringApplication.run(PerfilesApplication.class, args);
    }
}