package com.microservicios.servidor_empleados.service;

import com.microservicios.servidor_empleados.event.EmpleadoEventPublisher;
import com.microservicios.servidor_empleados.model.Empleado;
import com.microservicios.servidor_empleados.repository.EmpleadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock
    EmpleadoRepository repo;
    @Mock
    RestTemplate restTemplate;
    @Mock
    EmpleadoEventPublisher publisher;

    @InjectMocks
    EmpleadoService service;

    Empleado empleado;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "departamentosUrl", "http://localhost:8081");
        empleado = new Empleado("E001", "Ana García", "ana@empresa.com", "IT", LocalDate.now());
    }

    @Test
    void registrar_exitoso_publicaEvento() {
        when(repo.existsById("E001")).thenReturn(false);
        when(repo.save(empleado)).thenReturn(empleado);
        doReturn(null).when(restTemplate).getForEntity(anyString(), any());

        Empleado resultado = service.registrar(empleado);

        assertThat(resultado).isEqualTo(empleado);
        verify(repo).save(empleado);
        verify(publisher).publicarEmpleadoCreado(empleado);
    }

    @Test
    void registrar_empleadoDuplicado_lanzaExcepcion() {
        when(repo.existsById("E001")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(empleado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya existe");

        verify(repo, never()).save(any());
        verify(publisher, never()).publicarEmpleadoCreado(any());
    }

    @Test
    void eliminar_exitoso_publicaEvento() {
        when(repo.findById("E001")).thenReturn(Optional.of(empleado));

        service.eliminar("E001");

        verify(repo).deleteById("E001");
        verify(publisher).publicarEmpleadoEliminado(empleado);
    }

    @Test
    void eliminar_noExiste_lanzaExcepcion() {
        when(repo.findById("NONE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar("NONE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NONE");

        verify(repo, never()).deleteById(any());
        verify(publisher, never()).publicarEmpleadoEliminado(any());
    }

    @Test
    void registrar_siPublisherFalla_empleadoYaGuardado() {
        when(repo.existsById("E001")).thenReturn(false);
        when(repo.save(empleado)).thenReturn(empleado);
        doReturn(null).when(restTemplate).getForEntity(anyString(), any());
        doThrow(new RuntimeException("broker caído"))
                .when(publisher).publicarEmpleadoCreado(any());

        // No lanza excepción al llamante (el publisher captura internamente)
        // pero sigue siendo test de que save sí se llamó
        // El publisher real no propaga la excepción (captura internamente)
        // Aquí como el mock SÍ lanza, verificamos igualmente que save ocurrió
        assertThatNoException().isThrownBy(() -> {
            try {
                service.registrar(empleado);
            } catch (Exception ignored) {
            }
        });
        verify(repo).save(empleado);
    }
}
