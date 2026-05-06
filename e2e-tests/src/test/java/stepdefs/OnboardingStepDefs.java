package stepdefs;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import support.PollingUtils;
import support.TestContext;

import static io.restassured.RestAssured.given;

public class OnboardingStepDefs {

    private static final int POLLING_MAX_ATTEMPTS = 10;
    private static final int POLLING_INTERVAL_MS = 2000;

    @And("eventualment el usuario debe existir en el sistema de autenticación")
    public void verificarUsuarioEnAuth() {
        String email = TestContext.getUltimoEmpleadoEmail();
        System.out.println("Verificando usuario en auth-service para: " + email);

        boolean existe = PollingUtils.esperarLoginExitoso(
            email,
            "Temporal123",
            POLLING_MAX_ATTEMPTS,
            POLLING_INTERVAL_MS
        );

        Assert.assertTrue("El usuario debe existir en el sistema de autenticación después de varios intentos", existe);
    }

    @And("eventualment debe generarse una notificación de tipo {string} para el empleado")
    public void verificarNotificacion(String tipoNotificacion) {
        String empleadoId = TestContext.getUltimoEmpleadoId();
        System.out.println("Verificando notificación tipo '" + tipoNotificacion + "' para empleado: " + empleadoId);

        boolean encontrada = PollingUtils.esperarNotificacion(
            tipoNotificacion,
            POLLING_MAX_ATTEMPTS,
            POLLING_INTERVAL_MS
        );

        Assert.assertTrue("La notificación de tipo '" + tipoNotificacion + "' debe generarse", encontrada);
    }

    @And("eventualment el empleado puede iniciar sesión con su email y contraseña temporal")
    public void verificarLoginEmpleado() {
        String email = TestContext.getUltimoEmpleadoEmail();
        System.out.println("Verificando login para: " + email);

        boolean loginExitoso = PollingUtils.esperarLoginExitoso(
            email,
            "Temporal123",
            POLLING_MAX_ATTEMPTS,
            POLLING_INTERVAL_MS
        );

        Assert.assertTrue("El empleado debe poder iniciar sesión con credenciales temporales", loginExitoso);
    }

    @And("la respuesta del login debe contener un token de acceso")
    public void verificarTokenLogin() {
        String email = TestContext.getUltimoEmpleadoEmail();

        Response response = given()
            .contentType("application/json")
            .body("{\"email\": \"" + email + "\", \"password\": \"Temporal123\"}")
            .when()
            .post(TestContext.getBaseUrl() + "/auth/login");

        Assert.assertEquals("Login debe ser exitoso", 200, response.getStatusCode());

        String token = response.jsonPath().get("token");
        Assert.assertNotNull("Token debe existir", token);

        System.out.println("Login exitoso, token recibido");
    }
}