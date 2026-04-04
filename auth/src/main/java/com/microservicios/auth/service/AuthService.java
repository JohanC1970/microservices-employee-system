package com.microservicios.auth.service;

import com.microservicios.auth.dto.LoginRequest;
import com.microservicios.auth.dto.RecoverPasswordRequest;
import com.microservicios.auth.dto.ResetPasswordRequest;
import com.microservicios.auth.model.Usuario;
import com.microservicios.auth.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService,
                       PasswordEncoder passwordEncoder, RabbitTemplate rabbitTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Map<String, String> login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol().name());
        log.info("[AUTH] Login exitoso para: {}", usuario.getEmail());
        return Map.of("token", token, "rol", usuario.getRol().name());
    }

    public void recoverPassword(RecoverPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String tokenRecuperacion = jwtService.generarTokenRecuperacion(usuario.getEmail());
        log.info("[AUTH] Token de recuperación generado para: {}", usuario.getEmail());

        // Publicar evento usuario.recuperacion para que notificaciones envíe el email
        rabbitTemplate.convertAndSend(
                "auth.exchange",
                "usuario.recuperacion",
                Map.of(
                        "email", usuario.getEmail(),
                        "token", tokenRecuperacion
                )
        );
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!jwtService.esValido(request.getToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido o expirado");
        }

        Claims claims = jwtService.parsearToken(request.getToken());
        String tipo = (String) claims.get("tipo");
        if (!"RECUPERACION".equals(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token no es de recuperación");
        }

        String email = claims.getSubject();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
        log.info("[AUTH] Contraseña actualizada para: {}", email);
    }

    /** Llamado al consumir empleado.creado */
    public void crearUsuarioDesdeEmpleado(String empleadoId, String email, String nombre) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            log.warn("[AUTH] Usuario ya existe para email: {}", email);
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(""); // sin contraseña hasta que establezca una
        usuario.setRol(Usuario.Rol.USER);
        usuario.setActivo(true);
        usuario.setEmpleadoId(empleadoId);
        usuarioRepository.save(usuario);

        String tokenEstablecimiento = jwtService.generarTokenRecuperacion(email);
        log.info("[AUTH] Usuario creado para empleado: {} | email: {}", empleadoId, email);

        // Publicar evento usuario.creado para que notificaciones envíe instrucciones
        rabbitTemplate.convertAndSend(
                "auth.exchange",
                "usuario.creado",
                Map.of(
                        "email", email,
                        "nombre", nombre,
                        "token", tokenEstablecimiento
                )
        );
    }

    /** Llamado al consumir empleado.eliminado */
    public void desactivarUsuario(String empleadoId) {
        usuarioRepository.findByEmpleadoId(empleadoId).ifPresent(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            log.info("[AUTH] Usuario desactivado para empleadoId: {}", empleadoId);
        });
    }
}
