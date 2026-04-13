package com.microservicios.servidor_departamentos.service;

import com.microservicios.servidor_departamentos.model.Departamento;
import com.microservicios.servidor_departamentos.repository.DepartamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartamentoServiceTest {

    @Mock
    private DepartamentoRepository departamentoRepository;

    @InjectMocks
    private DepartamentoService departamentoService;

    private Departamento departamento;

    @BeforeEach
    void setUp() {
        departamento = new Departamento();
        departamento.setId("DEP001");
        departamento.setNombre("Ingenieria");
        departamento.setDescripcion("Departamento de ingenieria");
    }

    @Test
    void registrar_departamento_exitoso() {
        when(departamentoRepository.existsById("DEP001")).thenReturn(false);
        when(departamentoRepository.save(any(Departamento.class))).thenReturn(departamento);

        Departamento resultado = departamentoService.registrar(departamento);

        assertNotNull(resultado);
        assertEquals("DEP001", resultado.getId());
        verify(departamentoRepository, times(1)).save(departamento);
    }

    @Test
    void registrar_departamento_existente_lanza_excepcion() {
        when(departamentoRepository.existsById("DEP001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            departamentoService.registrar(departamento);
        });

        verify(departamentoRepository, never()).save(any());
    }

    @Test
    void obtener_departamento_existente() {
        when(departamentoRepository.findById("DEP001")).thenReturn(Optional.of(departamento));

        Departamento resultado = departamentoService.obtener("DEP001");

        assertNotNull(resultado);
        assertEquals("Ingenieria", resultado.getNombre());
    }

    @Test
    void obtener_departamento_no_existente_lanza_excepcion() {
        when(departamentoRepository.findById("DEP999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            departamentoService.obtener("DEP999");
        });
    }

    @Test
    void obtener_todos_los_departamentos() {
        List<Departamento> lista = List.of(departamento);
        when(departamentoRepository.findAll()).thenReturn(lista);

        List<Departamento> resultado = departamentoService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Ingenieria", resultado.get(0).getNombre());
    }

    @Test
    void obtener_todos_devuelve_lista_vacia() {
        when(departamentoRepository.findAll()).thenReturn(List.of());

        List<Departamento> resultado = departamentoService.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
