package com.microservicios.notificaciones.service;

import com.microservicios.notificaciones.model.Notificacion;
import com.microservicios.notificaciones.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository repository;

    public NotificacionService(NotificacionRepository repository) {
        this.repository = repository;
    }

    public Notificacion registrarBienvenida(String empleadoId, String nombre, String email) {
        String mensaje = "Bienvenido/a " + nombre + " a la empresa. Su cuenta ha sido creada exitosamente.";
        log.info("[NOTIFICACIÓN] Tipo: BIENVENIDA | Para: {}", email);
        return guardar("BIENVENIDA", email, mensaje, empleadoId);
    }

    public Notificacion registrarDesvinculacion(String empleadoId, String nombre, String email) {
        String mensaje = "Su cuenta ha sido desactivada. Gracias por su tiempo en la empresa, " + nombre + ".";
        log.info("[NOTIFICACIÓN] Tipo: DESVINCULACION | Para: {}", email);
        return guardar("DESVINCULACION", email, mensaje, empleadoId);
    }

    public Notificacion registrarEstablecimientoPassword(String email, String nombre, String token) {
        String mensaje = "Hola " + nombre + ", tu cuenta ha sido creada. "
                + "Usa el siguiente token para establecer tu contraseña: " + token;
        log.info("[NOTIFICACIÓN] Tipo: ESTABLECER_PASSWORD | Para: {}", email);
        return guardar("ESTABLECER_PASSWORD", email, mensaje, null);
    }

    public Notificacion registrarRecuperacionPassword(String email, String token) {
        String mensaje = "Solicitaste recuperar tu contraseña. "
                + "Usa el siguiente token para restablecerla: " + token;
        log.info("[NOTIFICACIÓN] Tipo: RECUPERACION_PASSWORD | Para: {}", email);
        return guardar("RECUPERACION_PASSWORD", email, mensaje, null);
    }

    public List<Notificacion> obtenerTodas() {
        return repository.findAll();
    }

    public List<Notificacion> obtenerPorEmpleado(String empleadoId) {
        return repository.findByEmpleadoId(empleadoId);
    }

    private Notificacion guardar(String tipo, String destinatario, String mensaje, String empleadoId) {
        Notificacion n = new Notificacion();
        n.setTipo(tipo);
        n.setDestinatario(destinatario);
        n.setMensaje(mensaje);
        n.setFechaEnvio(LocalDateTime.now());
        n.setEmpleadoId(empleadoId);
        return repository.save(n);
    }
}
