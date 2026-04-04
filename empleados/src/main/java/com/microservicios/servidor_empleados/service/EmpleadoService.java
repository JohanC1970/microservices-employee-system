package com.microservicios.servidor_empleados.service;

import com.microservicios.servidor_empleados.event.EmpleadoEventPublisher;
import com.microservicios.servidor_empleados.model.Empleado;
import com.microservicios.servidor_empleados.repository.EmpleadoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final RestTemplate restTemplate;
    private final EmpleadoEventPublisher eventPublisher;

    @Value("${departamentos.service.url}")
    private String departamentosUrl;

    public EmpleadoService(EmpleadoRepository empleadoRepository,
            RestTemplate restTemplate,
            EmpleadoEventPublisher eventPublisher) {
        this.empleadoRepository = empleadoRepository;
        this.restTemplate = restTemplate;
        this.eventPublisher = eventPublisher;
    }

    public Empleado registrar(Empleado empleado) {
        if (empleadoRepository.existsById(empleado.getId())) {
            throw new IllegalArgumentException("El empleado ya existe");
        }
        validarDepartamento(empleado.getDepartamentoId());
        Empleado guardado = empleadoRepository.save(empleado);
        // Publicar evento DESPUÉS de persistir – fallo no revierte la BD
        eventPublisher.publicarEmpleadoCreado(guardado);
        return guardado;
    }

    public Empleado obtener(String id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
    }

    public List<Empleado> obtenerTodos() {
        return empleadoRepository.findAll();
    }

    public void eliminar(String id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empleado con id " + id + " no encontrado"));
        empleadoRepository.deleteById(id);
        // Publicar evento DESPUÉS de eliminar – fallo no revierte la BD
        eventPublisher.publicarEmpleadoEliminado(empleado);
    }

    @Retry(name = "departamentoRetry")
    @CircuitBreaker(name = "departamentoCB", fallbackMethod = "fallbackDepartamento")
    private void validarDepartamento(String departamentoId) {
        String url = departamentosUrl + "/departamentos/" + departamentoId;
        restTemplate.getForEntity(url, Object.class);
    }

    private void fallbackDepartamento(String departamentoId, Exception ex) {
        if (ex instanceof HttpClientErrorException.NotFound) {
            throw new IllegalArgumentException(
                    "El departamento con id " + departamentoId + " no existe");
        }
        throw new RuntimeException("Servicio de departamentos no disponible");
    }
}