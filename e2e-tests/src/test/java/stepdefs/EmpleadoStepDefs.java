package stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import support.PollingUtils;
import support.TestContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class EmpleadoStepDefs {

    @Given("que existe un departamento {string} con nombre {string}")
    public void existeDepartamento(String deptId, String deptNombre) {
        Response response = given()
            .header("Authorization", "Bearer " + TestContext.getToken())
            .when()
            .get(TestContext.getBaseUrl() + "/departamentos/" + deptId);

        if (response.getStatusCode() == 200) {
            System.out.println("Departamento " + deptId + " existe");
        } else if (response.getStatusCode() == 404) {
            Map<String, Object> departamento = new HashMap<>();
            departamento.put("id", deptId);
            departamento.put("nombre", deptNombre);

            Response createResponse = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + TestContext.getToken())
                .body(departamento)
                .when()
                .post(TestContext.getBaseUrl() + "/departamentos");

            Assert.assertEquals("Departamento debe crearse", 201, createResponse.getStatusCode());
            System.out.println("Departamento " + deptId + " creado");
        }
    }

    @When("registro un empleado con los siguientes datos:")
    public void registrarEmpleado(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> datos = rows.get(0);

        String uniqueEmail = datos.get("email") + "." + System.currentTimeMillis() + ".test.com";

        Map<String, Object> empleado = new HashMap<>();
        empleado.put("nombre", datos.get("nombre"));
        empleado.put("email", uniqueEmail);
        empleado.put("departamentoId", datos.get("departamentoId"));

        TestContext.setLastRequestBody(empleado.toString());

        Response response = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestContext.getToken())
            .body(empleado)
            .when()
            .post(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
        System.out.println("Registro de empleado - Status: " + response.getStatusCode());
        System.out.println("Registro de empleado - Body: " + response.getBody().asString());

        if (response.getStatusCode() == 201) {
            String id = response.jsonPath().get("id");
            String email = response.jsonPath().get("email");
            TestContext.setUltimoEmpleadoId(id);
            TestContext.setUltimoEmpleadoEmail(uniqueEmail);
            System.out.println("Empleado creado con ID: " + id + ", Email: " + uniqueEmail);
        }
    }

    @When("registro un empleado con datos incompletos")
    public void registrarEmpleadoIncompleto() {
        Map<String, Object> empleado = new HashMap<>();
        empleado.put("nombre", "Empleado Incompleto");

        Response response = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestContext.getToken())
            .body(empleado)
            .when()
            .post(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
    }

    @When("creo un nuevo empleado con datos válidos")
    public void crearEmpleadoDatosValidos() {
        String uniqueEmail = "admin.test." + System.currentTimeMillis() + "@test.com";

        Map<String, Object> empleado = new HashMap<>();
        empleado.put("nombre", "Nuevo Empleado");
        empleado.put("email", uniqueEmail);
        empleado.put("departamentoId", "IT");

        Response response = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestContext.getToken())
            .body(empleado)
            .when()
            .post(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
    }

    @Then("la respuesta debe tener código {int}")
    public void verificarCodigo(int codigo) {
        Response response = TestContext.getLastResponse();
        Assert.assertEquals("Código de respuesta debe ser " + codigo, codigo, response.getStatusCode());
    }

    @And("el cuerpo debe contener el nombre {string}")
    public void verificarNombre(String nombre) {
        Response response = TestContext.getLastResponse();
        String nombreRespuesta = response.jsonPath().get("nombre");
        Assert.assertTrue("El nombre debe contener " + nombre,
            nombreRespuesta != null && nombreRespuesta.contains(nombre));
    }

    @And("guardo el ID del empleado creado")
    public void guardarIdEmpleado() {
        Response response = TestContext.getLastResponse();
        String id = response.jsonPath().get("id");
        TestContext.setUltimoEmpleadoId(id);
        System.out.println("ID guardado: " + id);
    }

    @When("elimino el empleado")
    public void eliminarEmpleado() {
        String id = TestContext.getUltimoEmpleadoId();

        Response response = given()
            .header("Authorization", "Bearer " + TestContext.getToken())
            .when()
            .delete(TestContext.getBaseUrl() + "/empleados/" + id);

        TestContext.setLastResponse(response);
        System.out.println("Eliminar empleado - Status: " + response.getStatusCode());
    }

    @When("intento eliminar el empleado")
    public void intentarEliminarEmpleado() {
        String id = TestContext.getUltimoEmpleadoId();

        Response response = given()
            .header("Authorization", "Bearer " + TestContext.getToken())
            .when()
            .delete(TestContext.getBaseUrl() + "/empleados/" + id);

        TestContext.setLastResponse(response);
    }

    @When("intento crear un nuevo empleado con datos válidos")
    public void intentarCrearEmpleado() {
        String uniqueEmail = "user.create." + System.currentTimeMillis() + "@test.com";

        Map<String, Object> empleado = new HashMap<>();
        empleado.put("nombre", "Empleado Usuario");
        empleado.put("email", uniqueEmail);
        empleado.put("departamentoId", "IT");

        Response response = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestContext.getToken())
            .body(empleado)
            .when()
            .post(TestContext.getBaseUrl() + "/empleados");

        TestContext.setLastResponse(response);
    }

    @When("intento eliminar un empleado con ID inexistente")
    public void eliminarInexistente() {
        Response response = given()
            .header("Authorization", "Bearer " + TestContext.getToken())
            .when()
            .delete(TestContext.getBaseUrl() + "/empleados/ID-INEXISTENTE-999");

        TestContext.setLastResponse(response);
    }

    @When("registro múltiples empleados de forma secuencial")
    public void registrarMultiplesEmpleados() {
        for (int i = 1; i <= 3; i++) {
            String uniqueEmail = "multi.test." + System.currentTimeMillis() + i + "@test.com";

            Map<String, Object> empleado = new HashMap<>();
            empleado.put("nombre", "Empleado Multiple " + i);
            empleado.put("email", uniqueEmail);
            empleado.put("departamentoId", "IT");

            Response response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + TestContext.getToken())
                .body(empleado)
                .when()
                .post(TestContext.getBaseUrl() + "/empleados");

            Assert.assertEquals("Empleado " + i + " debe crearse", 201, response.getStatusCode());
            System.out.println("Empleado multiple " + i + " creado: " + response.jsonPath().get("id"));
        }
    }

    @And("luego los elimino uno por uno")
    public void eliminarMultiples() {
        Response listResponse = given()
            .header("Authorization", "Bearer " + TestContext.getToken())
            .when()
            .get(TestContext.getBaseUrl() + "/empleados");

        List<String> ids = listResponse.jsonPath().get("id");

        for (String id : ids) {
            Response deleteResponse = given()
                .header("Authorization", "Bearer " + TestContext.getToken())
                .when()
                .delete(TestContext.getBaseUrl() + "/empleados/" + id);

            Assert.assertTrue("Eliminar debe ser 204 o 200", deleteResponse.getStatusCode() == 204 || deleteResponse.getStatusCode() == 200);
        }
    }

    @And("la lista debe contener al menos un empleado")
    public void verificarListaEmpleados() {
        Response response = TestContext.getLastResponse();
        List<?> empleados = response.jsonPath().get();
        Assert.assertTrue("La lista debe tener empleados", empleados != null && empleados.size() > 0);
    }

    @Given("que he registrado un empleado exitosamente")
    public void empleadoRegistradoExitosamente() {
        String uniqueEmail = "success.test." + System.currentTimeMillis() + "@test.com";

        Map<String, Object> empleado = new HashMap<>();
        empleado.put("nombre", "Empleado Exitoso");
        empleado.put("email", uniqueEmail);
        empleado.put("departamentoId", "IT");

        Response response = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestContext.getToken())
            .body(empleado)
            .when()
            .post(TestContext.getBaseUrl() + "/empleados");

        Assert.assertEquals("Registro debe ser exitoso", 201, response.getStatusCode());

        String id = response.jsonPath().get("id");
        TestContext.setUltimoEmpleadoId(id);
        TestContext.setUltimoEmpleadoEmail(uniqueEmail);
    }

    @Then("la respuesta debe tener código {int} o {int}")
    public void verificarCodigoAlternativo(int codigo1, int codigo2) {
        Response response = TestContext.getLastResponse();
        int actualCode = response.getStatusCode();
        boolean esValido = (actualCode == codigo1 || actualCode == codigo2);
        Assert.assertTrue("Código de respuesta debe ser " + codigo1 + " o " + codigo2 + " pero fue " + actualCode, esValido);
    }
}