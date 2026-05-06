package stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import support.PollingUtils;
import support.TestContext;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class OffboardingStepDefs {

    private static final int POLLING_MAX_ATTEMPTS = 10;
    private static final int POLLING_INTERVAL_MS = 2000;

    @Given("que existe un empleado registrado con credenciales activas")
    public void empleadoRegistradoConCredenciales() {
        String uniqueEmail = "offboard.test." + System.currentTimeMillis() + "@test.com";

        Map<String, Object> empleado = new HashMap<>();
        empleado.put("nombre", "Empleado Offboarding");
        empleado.put("email", uniqueEmail);
        empleado.put("departamentoId", "IT");

        Response createResponse = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestContext.getToken())
            .body(empleado)
            .when()
            .post(TestContext.getBaseUrl() + "/empleados");

        Assert.assertEquals("Empleado debe crearse", 201, createResponse.getStatusCode());

        String id = createResponse.jsonPath().get("id");
        TestContext.setUltimoEmpleadoId(id);
        TestContext.setUltimoEmpleadoEmail(uniqueEmail);
        TestContext.setUltimoEmpleadoPassword("Temporal123");

        System.out.println("Empleado registrado con credenciales - ID: " + id + ", Email: " + uniqueEmail);

        boolean loginExitoso = PollingUtils.esperarLoginExitoso(
            uniqueEmail,
            "Temporal123",
            POLLING_MAX_ATTEMPTS,
            POLLING_INTERVAL_MS
        );

        Assert.assertTrue("El empleado debe poder hacer login antes del offboarding", loginExitoso);
    }

    @When("elimino el empleado del sistema")
    public void eliminarEmpleadoSistema() {
        String id = TestContext.getUltimoEmpleadoId();

        Response response = given()
            .header("Authorization", "Bearer " + TestContext.getToken())
            .when()
            .delete(TestContext.getBaseUrl() + "/empleados/" + id);

        TestContext.setLastResponse(response);
        System.out.println("Offboarding - Status: " + response.getStatusCode());
    }

    @And("eventualment el empleado no puede iniciar sesión")
    public void verificarLoginFallido() {
        String email = TestContext.getUltimoEmpleadoEmail();
        System.out.println("Verificando que login falle para: " + email);

        boolean loginExitoso = false;
        for (int intento = 1; intento <= POLLING_MAX_ATTEMPTS; intento++) {
            try {
                Response response = given()
                    .contentType("application/json")
                    .body("{\"email\": \"" + email + "\", \"password\": \"Temporal123\"}")
                    .when()
                    .post(TestContext.getBaseUrl() + "/auth/login");

                if (response.getStatusCode() != 200 || response.jsonPath().get("token") == null) {
                    System.out.println("Login fallido como esperado en intento " + intento);
                    loginExitoso = false;
                    break;
                }
                loginExitoso = true;
            } catch (Exception e) {
                System.out.println("Intento " + intento + " fallido: " + e.getMessage());
            }

            if (intento < POLLING_MAX_ATTEMPTS) {
                try {
                    Thread.sleep(POLLING_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        Assert.assertFalse("El empleado desvinculado NO debe poder iniciar sesión", loginExitoso);
    }

    @And("solicito la recuperación de contraseña del empleado desvinculado")
    public void solicitarRecuperacion() {
        String email = TestContext.getUltimoEmpleadoEmail();

        Map<String, Object> request = new HashMap<>();
        request.put("email", email);

        Response response = given()
            .contentType("application/json")
            .body(request)
            .when()
            .post(TestContext.getBaseUrl() + "/auth/recover-password");

        TestContext.setLastResponse(response);
        System.out.println("Recuperación contraseña - Status: " + response.getStatusCode());
    }

    @Then("la solicitud debe fallar porque el usuario no está activo")
    public void verificarRecuperacionFallida() {
        Response response = TestContext.getLastResponse();

        System.out.println("Status de recuperación: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody().asString());

        Assert.assertTrue("La recuperación debe fallar (4xx) para usuario desvinculado",
            response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }

    @Then("cada eliminación debe ser exitosa")
    public void verificarEliminaciones() {
        Response response = TestContext.getLastResponse();
        Assert.assertTrue("Eliminación debe ser exitosa (204 o 200)",
            response.getStatusCode() == 204 || response.getStatusCode() == 200);
    }
}