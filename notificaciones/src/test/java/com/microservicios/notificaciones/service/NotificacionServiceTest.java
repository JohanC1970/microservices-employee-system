package com.microservicios.notificaciones.service;

import com.microservicios.notificaciones.model.Notificacion;
import com.microservicios.notificaciones.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    NotificacionRepository repository;

    @InjectMocks
    NotificacionService service;

    @Test
    void registrarBienvenida_guardaConTipoCorrecto() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = service.registrarBienvenida("E001", "Ana García", "ana@empresa.com");

        assertThat(resultado.getTipo()).isEqualTo("BIENVENIDA");
        assertThat(resultado.getDestinatario()).isEqualTo("ana@empresa.com");
        assertThat(resultado.getEmpleadoId()).isEqualTo("E001");
        assertThat(resultado.getMensaje()).contains("Ana García");
        assertThat(resultado.getFechaEnvio()).isNotNull();
    }

    @Test
    void registrarDesvinculacion_guardaConTipoCorrecto() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = service.registrarDesvinculacion("E001", "Ana García", "ana@empresa.com");

        assertThat(resultado.getTipo()).isEqualTo("DESVINCULACION");
        assertThat(resultado.getDestinatario()).isEqualTo("ana@empresa.com");
        assertThat(resultado.getEmpleadoId()).isEqualTo("E001");
        assertThat(resultado.getMensaje()).contains("Ana García");
    }

    @Test
    void registrarBienvenida_llamaSave() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.registrarBienvenida("E002", "Pedro", "pedro@empresa.com");

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("BIENVENIDA");
    }
}
