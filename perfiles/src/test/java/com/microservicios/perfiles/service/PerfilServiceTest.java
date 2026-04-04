package com.microservicios.perfiles.service;

import com.microservicios.perfiles.model.Perfil;
import com.microservicios.perfiles.repository.PerfilRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    PerfilRepository repository;

    @InjectMocks
    PerfilService service;

    Map<String, Object> evento = Map.of(
            "id", "E001",
            "nombre", "Ana García",
            "email", "ana@empresa.com");

    @Test
    void crearDesdeEvento_creaPerfilConCamposBasicos() {
        when(repository.existsByEmpleadoId("E001")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Perfil resultado = service.crearDesdeEvento(evento);

        assertThat(resultado.getEmpleadoId()).isEqualTo("E001");
        assertThat(resultado.getNombre()).isEqualTo("Ana García");
        assertThat(resultado.getEmail()).isEqualTo("ana@empresa.com");
        assertThat(resultado.getTelefono()).isEmpty();
        assertThat(resultado.getCiudad()).isEmpty();
        assertThat(resultado.getBiografia()).isEmpty();
        assertThat(resultado.getFechaCreacion()).isNotNull();
    }

    @Test
    void crearDesdeEvento_noCreaDuplicadoSiYaExiste() {
        Perfil existente = new Perfil();
        existente.setEmpleadoId("E001");
        when(repository.existsByEmpleadoId("E001")).thenReturn(true);
        when(repository.findByEmpleadoId("E001")).thenReturn(Optional.of(existente));

        Perfil resultado = service.crearDesdeEvento(evento);

        verify(repository, never()).save(any());
        assertThat(resultado.getEmpleadoId()).isEqualTo("E001");
    }

    @Test
    void obtenerPorEmpleado_lanzaExcepcionSiNoExiste() {
        when(repository.findByEmpleadoId("NONE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorEmpleado("NONE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NONE");
    }

    @Test
    void actualizar_modificaSoloCamposNoNulos() {
        Perfil existente = new Perfil();
        existente.setEmpleadoId("E001");
        existente.setTelefono("");
        existente.setCiudad("");
        when(repository.findByEmpleadoId("E001")).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Perfil actualizacion = new Perfil();
        actualizacion.setTelefono("3001234567");
        actualizacion.setCiudad("Armenia");

        Perfil resultado = service.actualizar("E001", actualizacion);

        assertThat(resultado.getTelefono()).isEqualTo("3001234567");
        assertThat(resultado.getCiudad()).isEqualTo("Armenia");
    }
}
