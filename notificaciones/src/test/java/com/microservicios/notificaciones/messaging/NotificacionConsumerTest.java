package com.microservicios.notificaciones.messaging;

import com.microservicios.notificaciones.model.Notificacion;
import com.microservicios.notificaciones.repository.NotificacionRepository;
import com.microservicios.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionConsumerTest {

    @Mock
    NotificacionRepository repo;

    @Mock
    NotificacionService notificacionService;

    @InjectMocks
    NotificacionConsumer consumer;

    @Test
    void consumirEmpleadoCreado_llamaRegistrarBienvenida() {
        Map<String, Object> evento = Map.of(
                "id", "E001",
                "nombre", "Ana García",
                "email", "ana@empresa.com",
                "departamentoId", "IT");

        consumer.onEmpleadoCreado(evento);

        verify(notificacionService).registrarBienvenida("E001", "Ana García", "ana@empresa.com");
    }

    @Test
    void consumirEmpleadoEliminado_llamaRegistrarDesvinculacion() {
        Map<String, Object> evento = Map.of(
                "id", "E001",
                "nombre", "Ana García",
                "email", "ana@empresa.com");

        consumer.onEmpleadoEliminado(evento);

        verify(notificacionService).registrarDesvinculacion("E001", "Ana García", "ana@empresa.com");
    }

    @Test
    void onMensaje_conDepartamentoId_llamaCreado() {
        Map<String, Object> evento = Map.of(
                "id", "E002",
                "nombre", "Luis Martínez",
                "email", "luis@empresa.com",
                "departamentoId", "RRHH");

        consumer.onMensaje(evento);

        verify(notificacionService).registrarBienvenida("E002", "Luis Martínez", "luis@empresa.com");
        verify(notificacionService, never()).registrarDesvinculacion(any(), any(), any());
    }

    @Test
    void onMensaje_sinDepartamentoId_llamaEliminado() {
        Map<String, Object> evento = Map.of(
                "id", "E003",
                "nombre", "Carlos Perez",
                "email", "carlos@empresa.com");

        consumer.onMensaje(evento);

        verify(notificacionService).registrarDesvinculacion("E003", "Carlos Perez", "carlos@empresa.com");
        verify(notificacionService, never()).registrarBienvenida(any(), any(), any());
    }
}
