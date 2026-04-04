# AGENTS Configuration

## Tasks

### run
Description: Levantar todos los servicios con Docker Compose

```bash
cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2" && docker-compose up --build -d
```

### stop
Description: Detener todos los servicios de Docker Compose

```bash
cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2" && docker-compose down
```

### build
Description: Compilar ambos microservicios

```bash
cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2\empleados" && mvn clean package -DskipTests && cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2\departamentos" && mvn clean package -DskipTests
```

### logs
Description: Ver logs de todos los contenedores

```bash
cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2" && docker-compose logs -f
```

### logs-empleados
Description: Ver logs del servicio de empleados

```bash
cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2" && docker-compose logs -f empleados-service
```

### logs-departamentos
Description: Ver logs del servicio de departamentos

```bash
cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2" && docker-compose logs -f departamentos-service
```

### status
Description: Ver estado de los contenedores

```bash
cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2" && docker-compose ps
```

### restart
Description: Reiniciar todos los servicios

```bash
cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2" && docker-compose restart
```

### clean
Description: Limpiar contenedores, volúmenes y builds

```bash
cd "E:\Uniquindio\Semestre 8\Microservicios\Reto 2" && docker-compose down -v --rmi local
```
