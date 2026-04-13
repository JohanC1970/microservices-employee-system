package com.microservicios.perfiles.service;

import com.microservicios.perfiles.model.Perfil;
import com.microservicios.perfiles.repository.PerfilRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PerfilService {

    private static final Logger logger = LoggerFactory.getLogger(PerfilService.class);
    private final PerfilRepository repository;

    public PerfilService(PerfilRepository repository) {
        this.repository = repository;
    }

    @CircuitBreaker(name = "perfilCB", fallbackMethod = "fallbackCrearPerfil")
    @Retry(name = "perfilRetry")
    public Perfil crearDesdeEvento(Map<String, Object> evento) {
        String empleadoId = (String) evento.get("id");

        if (repository.existsByEmpleadoId(empleadoId)) {
            return repository.findByEmpleadoId(empleadoId).orElseThrow();
        }

        Perfil perfil = new Perfil();
        perfil.setEmpleadoId(empleadoId);
        perfil.setNombre((String) evento.get("nombre"));
        perfil.setEmail((String) evento.get("email"));
        perfil.setTelefono("");
        perfil.setDireccion("");
        perfil.setCiudad("");
        perfil.setBiografia("");
        perfil.setFechaCreacion(LocalDateTime.now());
        return repository.save(perfil);
    }

    @CircuitBreaker(name = "perfilCB", fallbackMethod = "fallbackObtenerPerfil")
    @Retry(name = "perfilRetry")
    public Perfil obtenerPorEmpleado(String empleadoId) {
        return repository.findByEmpleadoId(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Perfil no encontrado para empleadoId: " + empleadoId));
    }

    @CircuitBreaker(name = "perfilCB", fallbackMethod = "fallbackListPerfil")
    @Retry(name = "perfilRetry")
    public List<Perfil> obtenerTodos() {
        return repository.findAll();
    }

    public void eliminarPorEmpleadoId(String empleadoId) {
        repository.findByEmpleadoId(empleadoId).ifPresent(repository::delete);
    }

    @CircuitBreaker(name = "perfilCB", fallbackMethod = "fallbackActualizarPerfil")
    @Retry(name = "perfilRetry")
    public Perfil actualizar(String empleadoId, Perfil datosActualizados) {
        Perfil perfil = obtenerPorEmpleado(empleadoId);

        if (datosActualizados.getTelefono() != null)
            perfil.setTelefono(datosActualizados.getTelefono());
        if (datosActualizados.getDireccion() != null)
            perfil.setDireccion(datosActualizados.getDireccion());
        if (datosActualizados.getCiudad() != null)
            perfil.setCiudad(datosActualizados.getCiudad());
        if (datosActualizados.getBiografia() != null)
            perfil.setBiografia(datosActualizados.getBiografia());

        return repository.save(perfil);
    }

    private Perfil fallbackCrearPerfil(Map<String, Object> evento, Throwable t) {
        logger.warn("CircuitBreaker activado al crear perfil, causa: {}", t.getMessage());
        return new Perfil();
    }

    private Perfil fallbackObtenerPerfil(String empleadoId, Throwable t) {
        logger.warn("CircuitBreaker activado al obtener perfil para empleado: {}, causa: {}", empleadoId, t.getMessage());
        return new Perfil();
    }

    private List<Perfil> fallbackListPerfil(Throwable t) {
        logger.warn("CircuitBreaker activado al listar perfiles, causa: {}", t.getMessage());
        return List.of();
    }

    private Perfil fallbackActualizarPerfil(String empleadoId, Perfil datos, Throwable t) {
        logger.warn("CircuitBreaker activado al actualizar perfil, causa: {}", t.getMessage());
        return new Perfil();
    }
}
