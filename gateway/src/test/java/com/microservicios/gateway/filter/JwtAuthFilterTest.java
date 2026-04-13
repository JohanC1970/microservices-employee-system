package com.microservicios.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;
    private String secretKey;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        secretKey = "mySecretKeyForJWTTokenGenerationMustBeLongEnough256Bits";
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
        jwtAuthFilter = new JwtAuthFilter(secretKey);
    }

    private String generateToken(String email, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .signWith(key)
                .compact();
    }

    @Test
    void filter_validToken_authentication_exitosa() {
        String token = generateToken("test@empresa.com", "USER");
        
        MockServerHttpRequest.BaseBuilder request = MockServerHttpRequest
                .get("/empleados")
                .header("Authorization", "Bearer " + token);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> resultado = jwtAuthFilter.filter(exchange, filterChain);

        assertNotNull(resultado);
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_noToken_devuelve_401() {
        MockServerHttpRequest.BaseBuilder request = MockServerHttpRequest
                .get("/empleados");
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> resultado = jwtAuthFilter.filter(exchange, filterChain);

        assertNotNull(resultado);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_invalidToken_devuelve_401() {
        MockServerHttpRequest.BaseBuilder request = MockServerHttpRequest
                .get("/empleados")
                .header("Authorization", "Bearer invalid-token");
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> resultado = jwtAuthFilter.filter(exchange, filterChain);

        assertNotNull(resultado);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_ruta_auth_permite_sin_token() {
        MockServerHttpRequest.BaseBuilder request = MockServerHttpRequest
                .get("/auth/login");
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> resultado = jwtAuthFilter.filter(exchange, filterChain);

        assertNotNull(resultado);
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_user_solo_get() {
        String token = generateToken("test@empresa.com", "USER");
        
        MockServerHttpRequest.BaseBuilder request = MockServerHttpRequest
                .post("/empleados")
                .header("Authorization", "Bearer " + token);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> resultado = jwtAuthFilter.filter(exchange, filterChain);

        assertNotNull(resultado);
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_admin_permite_todos_metodos() {
        String token = generateToken("admin@empresa.com", "ADMIN");
        
        MockServerHttpRequest.BaseBuilder request = MockServerHttpRequest
                .post("/empleados")
                .header("Authorization", "Bearer " + token);
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> resultado = jwtAuthFilter.filter(exchange, filterChain);

        assertNotNull(resultado);
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }
}
