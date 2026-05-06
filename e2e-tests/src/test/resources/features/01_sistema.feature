# language: es

Característica: Verificación del sistema

  Como sistema de pruebas automatizadas
  Quiero verificar que el sistema está desplegado y operativo
  Para poder ejecutar los escenarios de pruebas funcionales

  Escenario: El sistema responde correctamente mediante el API Gateway
    Cuando realizo una petición GET al endpoint de empleados sin autenticación
    Entonces la respuesta debe tener código 401
    Y la respuesta debe contener un mensaje de error indicando que no hay token

  Escenario: El API Gateway está accesible
    Dado que el sistema está desplegado y operativo
    Cuando realizo una petición GET al endpoint de empleados
    Entonces la respuesta debe tener código 401 o 200