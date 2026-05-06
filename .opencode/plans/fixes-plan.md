# Fixes Plan - Microservices Employee System

## Critical Issues Found in Audit

### Fix 1: Password Flow (CRITICAL)
**File:** `auth/src/main/java/com/microservicios/auth/service/AuthService.java:109`
**Problem:** New users created with empty password `""`, but e2e tests expect `"Temporal123"`
**Fix:** Change line 109 from:
```java
usuario.setPassword("");
```
to:
```java
usuario.setPassword(passwordEncoder.encode("Temporal123"));
```

### Fix 2: Empleado 404 Response (CRITICAL)
**File:** `empleados/src/main/java/com/microservicios/servidor_empleados/service/EmpleadoService.java:45-46`
**Problem:** Returns "Empleado no encontrado" without ID, mapped to 400 instead of 404
**Fix:** Change line 46 from:
```java
.orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
```
to:
```java
.orElseThrow(() -> new IllegalArgumentException("El empleado con id " + id + " no existe"));
```

**File:** `empleados/src/main/java/com/microservicios/servidor_empleados/Exception/GlobalExceptionHandler.java`
**Problem:** IllegalArgumentException returns 400, should return 404 for "not found" messages
**Fix:** Change handler to check message content or use ResponseStatusException instead

### Fix 3: Departamento 404 Response (CRITICAL)
**File:** `departamentos/src/main/java/com/microservicios/servidor_departamentos/service/DepartamentoService.java:35-37`
**Problem:** Returns 400 instead of 404 for not found
**Fix:** Change from IllegalArgumentException to ResponseStatusException:
```java
.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento no encontrado"));
```

**File:** `departamentos/src/main/java/com/microservicios/servidor_departamentos/controller/DepartamentoController.java:45`
**Update:** OpenAPI already says 404, just need service to match

### Fix 4: Unsupported Routes 404 Handler (CRITICAL - Reto 1)
**Files:** All services need `spring.mvc.throw-exception-if-no-handler-found=true` in application.properties
**Files:** All services need `spring.web.resources.add-mappings=false` in application.properties
**Add to each GlobalExceptionHandler:**
```java
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<String> handleNoResourceFound(NoResourceFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recurso no encontrado");
}
```

### Fix 5: Thread.sleep in AuthStepDefs (MODERATE - Reto 5)
**File:** `e2e-tests/src/test/java/stepdefs/AuthStepDefs.java:62-66`
**Problem:** Uses `Thread.sleep(3000)` instead of polling
**Fix:** Replace with PollingUtils.esperarLoginExitoso() or similar polling mechanism

### Fix 6: Empleado DELETE 404 Message
**File:** `empleados/src/main/java/com/microservicios/servidor_empleados/service/EmpleadoService.java:55-56`
**Problem:** Message doesn't match Reto requirement format
**Fix:** Ensure message includes the ID properly

## Implementation Priority
1. Fix 1 (Password) - Blocks all e2e tests
2. Fix 2 & 3 (404 responses) - Core API correctness
3. Fix 4 (Unsupported routes) - Reto 1 requirement
4. Fix 5 (Thread.sleep) - Reto 5 compliance
5. Fix 6 (DELETE message) - Minor consistency
