package com.microservicios.auth.config;

import com.microservicios.auth.model.Usuario;
import com.microservicios.auth.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initAdmin(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByEmail("admin@empresa.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setEmail("admin@empresa.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRol(Usuario.Rol.ADMIN);
                admin.setActivo(true);
                repo.save(admin);
                log.info("[AUTH] Usuario ADMIN creado: admin@empresa.com / admin123");
            }
        };
    }
}
