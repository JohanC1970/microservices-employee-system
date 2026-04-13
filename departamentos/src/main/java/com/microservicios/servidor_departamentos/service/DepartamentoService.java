package com.microservicios.servidor_departamentos.service;

import com.microservicios.servidor_departamentos.model.Departamento;
import com.microservicios.servidor_departamentos.repository.DepartamentoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoService {

    private static final Logger logger = LoggerFactory.getLogger(DepartamentoService.class);
    private final DepartamentoRepository departamentoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    @CircuitBreaker(name = "departamentoCB", fallbackMethod = "fallbackDepartamento")
    @Retry(name = "departamentoRetry")
    public Departamento registrar(Departamento departamento) {
        if (departamentoRepository.existsById(departamento.getId())) {
            throw new IllegalArgumentException("El departamento ya existe");
        }
        return departamentoRepository.save(departamento);
    }

    @CircuitBreaker(name = "departamentoCB", fallbackMethod = "fallbackDepartamento")
    @Retry(name = "departamentoRetry")
    public Departamento obtener(String id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Departamento no encontrado"));
    }

    @CircuitBreaker(name = "departamentoCB", fallbackMethod = "fallbackListDepartamento")
    @Retry(name = "departamentoRetry")
    public List<Departamento> obtenerTodos() {
        return departamentoRepository.findAll();
    }

    private Departamento fallbackDepartamento(Departamento departamento, Throwable t) {
        logger.warn("CircuitBreaker activado para departamento: {}, causa: {}", departamento.getId(), t.getMessage());
        return new Departamento();
    }

    private List<Departamento> fallbackListDepartamento(Throwable t) {
        logger.warn("CircuitBreaker activado para listar departamentos, causa: {}", t.getMessage());
        return List.of();
    }

}
