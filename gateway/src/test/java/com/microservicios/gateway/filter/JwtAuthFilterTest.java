package com.microservicios.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    private GatewayFilter gatewayFilter;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        String secretKey = "mySecretKeyForJWTTokenGenerationMustBeLongEnough256Bits";
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(secretKey);
        gatewayFilter = jwtAuthFilter.apply(new JwtAuthFilter.Config());
    }

    private String generateToken(String email, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("role", rol)
                .signWith(key)
                .compact();
    }

    @Test
    void filter_validToken_authentication_exitosa() {
        String token = generateToken("test@empresa.com", "USER");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/empleados")
                .header("Authorization", "Bearer " + token)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> resultado = gatewayFilter.filter(exchange, filterChain);

        assertNotNull(resultado);
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_noToken_devuelve_401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/empleados")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> resultado = gatewayFilter.filter(exchange, filterChain);
        StepVerifier.create(resultado).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_invalidToken_devuelve_401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/empleados")
                .header("Authorization", "Bearer invalid-token")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> resultado = gatewayFilter.filter(exchange, filterChain);
        StepVerifier.create(resultado).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_user_solo_get() {
        String token = generateToken("test@empresa.com", "USER");

        MockServerHttpRequest request = MockServerHttpRequest
                .post("/empleados")
                .header("Authorization", "Bearer " + token)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> resultado = gatewayFilter.filter(exchange, filterChain);
        StepVerifier.create(resultado).verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_admin_permite_todos_metodos() {
        String token = generateToken("admin@empresa.com", "ADMIN");

        MockServerHttpRequest request = MockServerHttpRequest
                .post("/empleados")
                .header("Authorization", "Bearer " + token)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> resultado = gatewayFilter.filter(exchange, filterChain);

        assertNotNull(resultado);
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
    }
}
