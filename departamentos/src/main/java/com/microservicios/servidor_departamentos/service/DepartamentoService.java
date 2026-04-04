package com.microservicios.servidor_departamentos.service;

import com.microservicios.servidor_departamentos.model.Departamento;
import com.microservicios.servidor_departamentos.repository.DepartamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    public Departamento registrar(Departamento departamento) {
        if (departamentoRepository.existsById(departamento.getId())) {
            throw new IllegalArgumentException("El departamento ya existe");
        }
        return departamentoRepository.save(departamento);
    }

    public Departamento obtener(String id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Departamento no encontrado"));
    }

    public List<Departamento> obtenerTodos() {
        return departamentoRepository.findAll();
    }

}
