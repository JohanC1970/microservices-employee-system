package stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import support.TestContext;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class AuthStepDefs {

    @Given("que estoy autenticado como administrador del sistema")
    public void autenticadoComoAdmin() {
        Response response = given()
            .contentType("application/json")
            .body("{\"email\": \"" + TestContext.getAdminEmail() + "\", \"password\": \"" + TestContext.getAdminPassword() + "\"}")
            .when()
            .post(TestContext.getBaseUrl() + "/auth/login");

        Assert.assertEquals("Login de admin debe ser exitoso", 200, response.getStatusCode());

        String token = response.jsonPath().get("token");
        Assert.assertNotNull("Token no debe ser null", token);

        TestContext.setToken(token);
        System.out.println("Admin autenticado con token: " + token.substring(0, 20) + "...");
    }

    @Given("que tengo un token de autenticación inválido")
    public void tokenInvalido() {
        TestContext.setToken("token_invalido_12345");
    }

    @Given("que tengo un token de autenticación malformado")
    public void tokenMalformado() {
        TestContext.setToken("Bearer token_malformado_sin_formato_jwt");
    }

    @Given("que estoy autenticado con rol USER")
    public void autenticadoComoUser() {
        String uniqueEmail = "user.test." + System.currentTimeMillis() + "@test.com";

        Map<String, Object> empleado = new HashMap<>();
        empleado.put("nombre", "Usuario Test");
        empleado.put("email", uniqueEmail);
        empleado.put("departamentoId", "IT");

        Response createResponse = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestContext.getToken())
            .body(empleado)
            .when()
            .post(TestContext.getBaseUrl() + "/empleados");

        if (createResponse.getStatusCode() == 201) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Response loginResponse = given()
                .contentType("application/json")
                .body("{\"email\": \"" + uniqueEmail + "\", \"password\": \"Temporal123\"}")
                .when()
                .post(TestContext.getBaseUrl() + "/auth/login");

            if (loginResponse.getStatusCode() == 200) {
                String userToken = loginResponse.jsonPath().get("token");
                TestContext.setToken(userToken);
                TestContext.setUltimoEmpleadoEmail(uniqueEmail);
                System.out.println("Usuario USER autenticado");
                return;
            }
        }

        Assert.fail("No se pudo crear usuario con rol USER para la prueba");
    }

    @Given("que estoy autenticado como administrador")
    public void autenticadoComoAdmin2() {
        if (TestContext.getToken() == null || TestContext.getToken().isEmpty()) {
            autenticadoComoAdmin();
        }
    }

    @And("que existe un empleado de prueba")
    public void existeEmpleadoDePrueba() {
        String uniqueEmail = "empleado.test." + System.currentTimeMillis() + "@test.com";

        Map<String, Object> empleado = new HashMap<>();
        empleado.put("nombre", "Empleado Prueba");
        empleado.put("email", uniqueEmail);
        empleado.put("departamentoId", "IT");

        Response response = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestContext.getToken())
            .body(empleado)
            .when()
            .post(TestContext.getBaseUrl() + "/empleados");

        Assert.assertEquals("Crear empleado de prueba debe ser exitoso", 201, response.getStatusCode());

        String id = response.jsonPath().get("id");
        TestContext.setUltimoEmpleadoId(id);
        System.out.println("Empleado de prueba creado con ID: " + id);
    }

    @When("consulto la lista de empleados sin token de autenticación")
    public void consultarSinToken() {
        Response response = RestAssured
            .given()
            .when()
            .get(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
        System.out.println("Respuesta sin token: " + response.getStatusCode());
    }

    @When("consulto la lista de empleados con token inválido")
    public void consultarConTokenInvalido() {
        Response response = RestAssured
            .given()
            .header("Authorization", "Bearer " + TestContext.getToken())
            .when()
            .get(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
    }

    @When("consulto la lista de empleados con token malformado")
    public void consultarConTokenMalformado() {
        Response response = RestAssured
            .given()
            .header("Authorization", TestContext.getToken())
            .when()
            .get(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
    }

    @When("consulto la lista de empleados")
    public void consultarEmpleados() {
        Response response = given()
            .header("Authorization", "Bearer " + TestContext.getToken())
            .when()
            .get(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
    }

    @Given("que el sistema está desplegado y operativo")
    public void sistemaOperativo() {
        Response response = given()
            .when()
            .get(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
        System.out.println("Sistema verificado - Código de respuesta: " + response.getStatusCode());
    }

    @When("realizo una petición GET al endpoint de empleados sin autenticación")
    public void peticionSinAutenticacion() {
        Response response = RestAssured
            .given()
            .when()
            .get(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
        System.out.println("Petición sin auth - Código: " + response.getStatusCode());
    }

    @When("realizo una petición GET al endpoint de empleados")
    public void peticionSimple() {
        Response response = RestAssured
            .given()
            .when()
            .get(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
    }

    @io.cucumber.java.en.Then("la respuesta debe contener un mensaje de error indicando que no hay token")
    public void verificarMensajeSinToken() {
        Response response = TestContext.getLastResponse();
        String body = response.getBody().asString();
        boolean containsError = body.toLowerCase().contains("token") || 
                                 body.toLowerCase().contains("unauthorized") ||
                                 body.toLowerCase().contains("no autorizado") ||
                                 body.toLowerCase().contains("access denied");
        
        Assert.assertTrue("La respuesta debe contener mensaje de error sobre token", containsError);
    }
}