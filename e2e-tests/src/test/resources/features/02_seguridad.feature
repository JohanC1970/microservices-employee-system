# language: es

Característica: Seguridad y control de acceso

  Como sistema de autenticación
  Quiero controlar el acceso a los recursos
  Para garantizar que solo los usuarios autorizados realicen operaciones

  Antecedentes:
    Dado que estoy autenticado como administrador del sistema

  @security
  Escenario: Acceso denegado sin token de autenticación
    Cuando consulto la lista de empleados sin token de autenticación
    Entonces la respuesta debe tener código 401

  @security
  Escenario: Acceso denegado con token inválido
   Dado que tengo un token de autenticación inválido
    Cuando consulto la lista de empleados con token inválido
    Entonces la respuesta debe tener código 401

  @security
  Escenario: Acceso denegado con token malformado
    Dado que tengo un token de autenticación malformado
    Cuando consulto la lista de empleados con token malformado
    Entonces la respuesta debe tener código 401

  @rbac
  Escenario: Usuario con rol USER no puede crear empleados
    Dado que estoy autenticado con rol USER
    Cuando intento crear un nuevo empleado con datos válidos
    Entonces la respuesta debe tener código 403

  @rbac
  Escenario: Usuario con rol USER no puede eliminar empleados
    Dado que estoy autenticado con rol USER
    Y que existe un empleado de prueba
    Cuando intento eliminar el empleado
    Entonces la respuesta debe tener código 403

  @rbac
  Escenario: Usuario con rol USER puede consultar empleados
    Dado que estoy autenticado con rol USER
    Cuando consulto la lista de empleados
    Entonces la respuesta debe tener código 200

  @rbac
  Escenario: Usuario con rol ADMIN puede crear empleados
    Dado que estoy autenticado como administrador
    Cuando creo un nuevo empleado con datos válidos
    Entonces la respuesta debe tener código 201

  @rbac
  Escenario: Usuario con rol ADMIN puede eliminar empleados
    Dado que estoy autenticado como administrador
    Y que existe un empleado de prueba
    Cuando elimino el empleado
    Entonces la respuesta debe tener código 204