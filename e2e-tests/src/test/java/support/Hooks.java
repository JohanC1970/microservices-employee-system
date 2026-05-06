package support;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;

public class Hooks {

    @Before
    public void setUp() {
        String baseUrl = TestContext.getBaseUrl();
        RestAssured.baseURI = baseUrl;
        System.out.println("Configurando prueba con BASE_URL: " + baseUrl);
    }

    @After
    public void tearDown() {
        System.out.println("Limpiando contexto después del escenario");
        TestContext.clear();
    }
}