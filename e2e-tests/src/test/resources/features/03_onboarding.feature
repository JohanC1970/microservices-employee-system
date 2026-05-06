# language: es

Característica: Onboarding de empleados

  Como administrador del sistema
  Quiero registrar nuevos empleados
  Para iniciar su proceso de onboarding

  Antecedentes:
    Dado que existe un departamento "IT" con nombre "Tecnología"
    Y que estoy autenticado como administrador

  @onboarding
  Escenario: Registro exitoso de un empleado y creación automática de credenciales
    Cuando registro un empleado con los siguientes datos:
      | nombre     | email                       | departamentoId |
      | Juan Pérez | juan.perez.onboard@test.com | IT             |
    Entonces la respuesta debe tener código 201
    Y el cuerpo debe contener el nombre "Juan Pérez"
    Y guardo el ID del empleado creado
    Y eventualment el usuario debe existir en el sistema de autenticación

  @onboarding
  Escenario: Registro exitoso genera notificación de bienvenida
    Cuando registro un empleado con los siguientes datos:
      | nombre           | email                         | departamentoId |
      | María González   | maria.gonzalez@test.com       | IT             |
    Entonces la respuesta debe tener código 201
    Y eventualment debe generarse una notificación de tipo "bienvenida" para el empleado

  @onboarding
  @login
  Escenario: El nuevo empleado puede establecer su contraseña y hacer login exitosamente
    Cuando registro un empleado con los siguientes datos:
      | nombre         | email                    | departamentoId |
      | Pedro López     | pedro.lopez@test.com     | IT             |
    Entonces la respuesta debe tener código 201
    Y eventualment el empleado puede iniciar sesión con su email y contraseña temporal
    Y la respuesta del login debe contener un token de acceso

  @onboarding
  Escenario: Registro con departamento inexistente debe fallar
    Cuando registro un empleado con los siguientes datos:
      | nombre   | email              | departamentoId |
      | Test     | test@empresa.com   | DEPT-INVALIDO  |
    Entonces la respuesta debe tener código 400

  @onboarding
  Escenario: Registro con campos faltantes debe fallar
    Cuando registro un empleado con datos incompletos
    Entonces la respuesta debe tener código 400

@onboarding
  Escenario: Listar empleados después de registro exitoso
    Dado que he registrado un empleado exitosamente
    Cuando consulto la lista de empleados
    Entonces la respuesta debe tener código 200
    Y la lista debe contener al menos un empleado