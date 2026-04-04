package com.microservicios.perfiles.repository;

import com.microservicios.perfiles.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, String> {
    Optional<Perfil> findByEmpleadoId(String empleadoId);

    boolean existsByEmpleadoId(String empleadoId);
}
