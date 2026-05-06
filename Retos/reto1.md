**Reto 1 – Servidor Web para Gestión Básica de Empleados** 

**Contexto** 

Este reto hace parte de una serie de ejercicios progresivos cuyo objetivo final es construir un **sistema de onboarding y offboarding de empleados**, basado en una **arquitectura orientada a microservicios**. 

En este primer reto se trabajará una versión mínima del sistema, enfocada en: 

Desarrollo de servicios web 

Manejo básico de solicitudes HTTP 

Definición de una estrategia inicial de despliegue 

**Objetivo** 

Iniciar el desarrollo de un sistema backend mediante la construcción de un **servicio web simple**, junto con su **estrategia de despliegue en contenedores**, que sirva como base para retos posteriores donde se incorporarán nuevos servicios, reglas de negocio y capacidades arquitectónicas. 

**Requisitos generales** 

La solución desarrollada debe cumplir con los siguientes requisitos: 

Implementar un servidor web accesible desde http://localhost . 

Exponer endpoints HTTP para el registro y consulta de empleados. 

Manejar correctamente rutas, métodos HTTP y códigos de estado. 

Permitir la ejecución de la aplicación dentro de un contenedor Docker. 

Facilitar su prueba mediante herramientas externas de consumo de APIs.  
**1\. Registro de un empleado** 

**Ruta:** 

POST http://localhost/empleados 

**Descripción:** 

Esta operación permite registrar un empleado en el sistema. 

**Entrada:** 

El cuerpo de la solicitud debe contener la información del empleado 

(por ejemplo: id , nombre , cargo ). 

**Respuesta exitosa:** 

Código de estado: 200 OK 

Cuerpo de la respuesta: la información del empleado registrado. 

**2\. Consulta de un empleado por id** 

**Ruta:** 

GET http://localhost/empleados/{id} 

**Descripción:** 

Esta operación permite consultar la información de un empleado a partir de su identificador. **Respuesta exitosa:** 

Código de estado: 200 OK 

Cuerpo de la respuesta: la información del empleado correspondiente al {id} solicitado. **Si el empleado no existe:** 

Código de estado: 404 Not Found 

Cuerpo de la respuesta: 

El empleado con id {id} no existe 

**3\. Rutas no soportadas** 

Para cualquier otra ruta o método HTTP no definido anteriormente, el servidor debe responder: Código de estado: 404 Not Found  
Cuerpo de la respuesta: 

Recurso no encontrado 

**4\. Pruebas del servidor** 

Verifique el correcto funcionamiento de su aplicación utilizando alguna de las siguientes herramientas: 

Bruno 

curl 

Postman 

u otra herramienta similar para realizar solicitudes HTTP 

**5\. Contenerización de la aplicación** 

Cree un contenedor para el despliegue de la aplicación utilizando **Docker**. La solución debe cumplir con los siguientes requisitos: 

Incluir un archivo Dockerfile en el proyecto. 

Exponer el puerto en el que se ejecuta el servidor. 

Permitir la ejecución de la aplicación mediante los comandos: 

docker build \-t servidor-empleados . 

docker run \-p 8080:8080 servidor-empleados 

El puerto puede ajustarse según la implementación del estudiante 

⸻ 

Consideraciones 

• No es obligatorio el uso de base de datos. 

• Los empleados pueden almacenarse en memoria utilizando estructuras de datos simples. • Este servicio será extendido y complementado en retos posteriores.  
• Las decisiones tomadas en este reto servirán como base para la evolución hacia una arquitectura orientada a microservicios.