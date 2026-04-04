# Guía de Presentación - Reto 3: Arquitectura Orientada a Eventos

Esta guía contiene el paso a paso detallado para presentar el funcionamiento del sistema de microservicios y la integración con RabbitMQ a tu profesor.

---

## 1. Explicación de la Arquitectura (El "Qué" y el "Por qué")

**Objetivo:** Mostrar que entiendes cómo interactúan los componentes.

*   **Punto clave:** "Profesor, evolucionamos el sistema del Reto 2. Antes los microservicios se comunicaban de forma síncrona (esperando respuesta). Ahora implementamos una arquitectura orientada a eventos usando **RabbitMQ**."
*   **El patrón utilizado:** "Aplicamos el patrón **Fan-Out** mediante un `TopicExchange`. Esto permite que el microservicio de Empleados publique un único evento (ej. `empleado.creado`) y que múltiples servicios (*Notificaciones* y *Perfiles*) lo escuchen y reaccionen de forma independiente."
*   **Ventajas:** "Esto desacopla los servicios. Si *Notificaciones* se cae, el registro del empleado no falla; el mensaje se guarda en la cola hasta que el servicio vuelva a levantarse."

---

## 2. Demostración en Vivo: Paso a Paso

*Asegúrate de tener todos los contenedores corriendo (puedes mostrar la terminal con `docker-compose ps`)*.

### Paso 2.1: Crear la dependencia (Departamento)
*   **Acción:** Ejecuta la creación de un departamento.
    ```bash
    curl -s -X POST http://localhost:8081/departamentos \
         -H "Content-Type: application/json" \
         -d '{"id":"VE","nombre":"Ventas","descripcion":"Departamento de Ventas"}'
    ```
*   **Explicación:** "Primero creamos un departamento, requisito para registrar empleados, tal como lo hacíamos en el Reto 2."

### Paso 2.2: Crear el Empleado (El detonante del evento)
*   **Acción:** Crea el empleado Carlos Lopez.
    ```bash
    curl -s -X POST http://localhost:8080/empleados \
         -H "Content-Type: application/json" \
         -d '{"id":"E005","nombre":"Carlos Lopez","email":"carlos@empresa.com","departamentoId":"VE","fechaIngreso":"2023-05-10"}'
    ```
*   **Explicación:** "Al ejecutar esto, el microservicio de Empleados guarda a Carlos en su base de datos y, **de forma asíncrona**, publica el evento `empleado.creado` en RabbitMQ enviando sus datos esenciales (ID, nombre, correo)."

### Paso 2.3: Mostrar el trabajo de los Consumidores (Servicios Autónomos)

**A. El servicio de Perfiles:**
*   **Acción:** Consulta el perfil recién creado automáticamente.
    ```bash
    curl -s http://localhost:8083/perfiles/E005
    ```
*   **Explicación:** "Vemos que el servicio de Perfiles escuchó el evento y le creó un perfil por defecto a Carlos de forma automática. Campos como 'telefono' o 'ciudad' están vacíos listos para que él los complete."

**B. El servicio de Notificaciones:**
*   **Acción:** Verifica el historial de notificaciones.
    ```bash
    curl -s http://localhost:8084/notificaciones/E005
    ```
*   **Explicación:** "Simultáneamente, el microservicio de Notificaciones también escuchó el evento `empleado.creado` (Fan-Out) y guardó un registro de tipo `BIENVENIDA`, simulando el envío de un correo."

---

## 3. Demostración de otro Evento: Eliminación

*   **Acción:** Borrar el empleado.
    ```bash
    curl -s -X DELETE http://localhost:8080/empleados/E005
    ```
*   **Explicación:** "Ahora vamos a eliminar a Carlos. El servicio de Empleados lo borra de su tabla y publica el evento `empleado.eliminado`."

*   **Acción:** Revisar las notificaciones de nuevo.
    ```bash
    curl -s http://localhost:8084/notificaciones/E005
    ```
*   **Explicación:** "Si revisamos de nuevo sus notificaciones, vemos que se añadió un nuevo registro de tipo `DESVINCULACION`. En este caso, el servicio de Perfiles no está suscrito a este evento, demostrando cómo podemos enrutar mensajes solo a quienes les interesa."

---

## 4. Mostrar el Broker (RabbitMQ Management Interface)

**Objetivo:** Demostrar que dominas la infraestructura debajo de los servicios.

1.  Abre el navegador en [http://localhost:15672](http://localhost:15672) (Usuario/Clave: `admin` / `admin`).
2.  Ve a la pestaña **Exchanges**.
3.  Busca y haz clic en `empleados.exchange`.
4.  Baja hasta **Bindings**.
5.  **Explicación:** "Profesor, aquí podemos ver el enrutamiento (routing keys). Vemos que tanto la cola `notificaciones.queue` como `perfiles.queue` están enlazadas a la routing key `empleado.creado`, mientras que solo la de notificaciones está enlazada a `empleado.eliminado`. Esta es la implementación visual del patrón Fan-Out."

---

## 5. Cierre: Muestra del Código (Opcional si lo pide)

Si el profesor pide ver el código, muéstrale los siguientes 3 archivos clave:

1.  `empleados-service/src/.../RabbitMQConfig.java`: Para demostrar cómo se define el `TopicExchange` y los `Binding`.
2.  `empleados-service/src/.../EmpleadoEventPublisher.java`: Muestra la inyección de `RabbitTemplate` y cómo el método `convertAndSend` emite el evento al exchange *sin frenar* el hilo principal.
3.  `notificaciones-service/src/.../NotificacionConsumer.java`: Muestra cómo se usa la anotación `@RabbitListener` para suscribirse a la cola y procesar el JSON entrante.
