package com.microservicios.perfiles.service;

import com.microservicios.perfiles.model.Perfil;
import com.microservicios.perfiles.repository.PerfilRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PerfilService {

    private final PerfilRepository repository;

    public PerfilService(PerfilRepository repository) {
        this.repository = repository;
    }

    /** Crea perfil por defecto a partir de un evento empleado.creado */
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

    public Perfil obtenerPorEmpleado(String empleadoId) {
        return repository.findByEmpleadoId(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Perfil no encontrado para empleadoId: " + empleadoId));
    }

    public List<Perfil> obtenerTodos() {
        return repository.findAll();
    }

    public void eliminarPorEmpleadoId(String empleadoId) {
        repository.findByEmpleadoId(empleadoId).ifPresent(repository::delete);
    }

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
}
