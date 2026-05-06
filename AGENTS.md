# AGENTS Configuration

## Project Structure
- **e2e-tests/**: Proyecto de pruebas BDD con Cucumber-JVM
- **empleados/**: Microservicio de empleados
- **departamentos/**: Microservicio de departamentos
- **auth/**: Servicio de autenticación
- **gateway/**: API Gateway con Spring Cloud Gateway
- **eureka/**: Servicio de descubrimiento Eureka
- **notificaciones/**: Servicio de notificaciones
- **perfiles/**: Servicio de perfiles

## URLs de Servicios
- **API Gateway**: http://localhost:8085
- **Auth Service (directo)**: http://localhost:8082
- **Empleados Service**: http://localhost:8081
- **Departamentos Service**: http://localhost:8083

## Tasks

### run
Description: Levantar todos los servicios con Docker Compose

```bash
cd "E:\GitHub\microservices-employee-system" && docker-compose up --build -d
```

### stop
Description: Detener todos los servicios de Docker Compose

```bash
cd "E:\GitHub\microservices-employee-system" && docker-compose down
```

### build
Description: Compilar todos los microservicios

```bash
cd "E:\GitHub\microservices-employee-system" && mvn clean package -DskipTests -q
```

### logs
Description: Ver logs de todos los contenedores

```bash
cd "E:\GitHub\microservices-employee-system" && docker-compose logs -f
```

### status
Description: Ver estado de los contenedores

```bash
cd "E:\GitHub\microservices-employee-system" && docker-compose ps
```

### restart
Description: Reiniciar todos los servicios

```bash
cd "E:\GitHub\microservices-employee-system" && docker-compose restart
```

### clean
Description: Limpiar contenedores, volúmenes y builds

```bash
cd "E:\GitHub\microservices-employee-system" && docker-compose down -v --rmi local
```

### test-e2e
Description: Ejecutar pruebas BDD

```bash
cd "E:\GitHub\microservices-employee-system\e2e-tests" && mvn test
```

### test-e2e-run
Description: Ejecutar pruebas BDD con Cucumber reporting

```bash
cd "E:\GitHub\microservices-employee-system\e2e-tests" && mvn verify
```