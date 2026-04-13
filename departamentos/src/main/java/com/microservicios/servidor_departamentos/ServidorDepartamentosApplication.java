package com.microservicios.servidor_departamentos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ServidorDepartamentosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServidorDepartamentosApplication.class, args);
	}

}
