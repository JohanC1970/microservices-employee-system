package com.microservicios.notificaciones.repository;

import com.microservicios.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, String> {
    List<Notificacion> findByEmpleadoId(String empleadoId);
}
