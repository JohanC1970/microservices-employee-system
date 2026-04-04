package com.microservicios.servidor_empleados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ServidorEmpleadosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServidorEmpleadosApplication.class, args);
	}

}
