package com.microservicios.auth.service;

import com.microservicios.auth.dto.LoginRequest;
import com.microservicios.auth.model.Usuario;
import com.microservicios.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setEmail("test@empresa.com");
        usuario.setPassword("encodedPassword");
        usuario.setRol(Usuario.Rol.USER);
        usuario.setActivo(true);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@empresa.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void login_exitoso() {
        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generarToken(anyString(), anyString())).thenReturn("jwt-token");

        Map<String, String> resultado = authService.login(loginRequest);

        assertNotNull(resultado);
        assertEquals("jwt-token", resultado.get("token"));
        assertEquals("USER", resultado.get("rol"));
    }

    @Test
    void login_usuario_no_existe() {
        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void login_usuario_inactivo() {
        usuario.setActivo(false);
        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void login_password_incorrecto() {
        when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
