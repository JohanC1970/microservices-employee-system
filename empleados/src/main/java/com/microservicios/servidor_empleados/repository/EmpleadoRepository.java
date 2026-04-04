package com.microservicios.servidor_empleados.repository;

import com.microservicios.servidor_empleados.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado,String> {

}
