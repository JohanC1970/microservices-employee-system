# language: es

Característica: Offboarding de empleados

  Como administrador del sistema
  Quiero desvincular empleados del sistema
  Para completar su proceso de offboarding

  Antecedentes:
    Dado que existe un departamento "IT" con nombre "Tecnología"
    Y que estoy autenticado como administrador

  @offboarding
  Escenario: Desvinculación completa de un empleado
    Y que existe un empleado registrado con credenciales activas
    Cuando elimino el empleado del sistema
    Entonces la respuesta debe tener código 204
    Y eventualment debe generarse una notificación de tipo "desvinculación" para el empleado

  @offboarding
  Escenario: Empleado desvinculado no puede hacer login
    Y que existe un empleado registrado con credenciales activas
    Cuando elimino el empleado del sistema
    Entonces eventualment el empleado no puede iniciar sesión

  @offboarding
  Escenario: Recuperación de contraseña falla para empleado desvinculado
    Y que existe un empleado registrado con credenciales activas
    Cuando elimino el empleado del sistema
    Y solicito la recuperación de contraseña del empleado desvinculado
    Entonces la solicitud debe fallar porque el usuario no está activo

  @offboarding
  Escenario: Eliminar empleado que no existe
    Cuando intento eliminar un empleado con ID inexistente
    Entonces la respuesta debe tener código 404

  @offboarding
  Escenario: Offboarding múltiples empleados de forma independiente
    Cuando registro múltiples empleados de forma secuencial
    Y luego los elimino uno por uno
    Entonces cada eliminación debe ser exitosa