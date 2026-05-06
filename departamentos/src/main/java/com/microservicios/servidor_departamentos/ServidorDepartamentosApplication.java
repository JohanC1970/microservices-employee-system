package com.microservicios.servidor_departamentos;

import com.microservicios.servidor_departamentos.model.Departamento;
import com.microservicios.servidor_departamentos.repository.DepartamentoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ServidorDepartamentosApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServidorDepartamentosApplication.class, args);
    }

    @Bean
    CommandLineRunner initDepartamentos(DepartamentoRepository repo) {
        return args -> {
            if (!repo.existsById("IT")) {
                Departamento it = new Departamento();
                it.setId("IT");
                it.setNombre("Tecnología");
                repo.save(it);
            }
            if (!repo.existsById("HR")) {
                Departamento hr = new Departamento();
                hr.setId("HR");
                hr.setNombre("Recursos Humanos");
                repo.save(hr);
            }
        };
    }
}