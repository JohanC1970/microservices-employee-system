package support;

import io.restassured.response.Response;

public class TestContext {
    private static final ThreadLocal<String> token = new ThreadLocal<>();
    private static final ThreadLocal<Response> lastResponse = new ThreadLocal<>();
    private static final ThreadLocal<String> lastRequestBody = new ThreadLocal<>();
    private static final ThreadLocal<String> ultimoEmpleadoId = new ThreadLocal<>();
    private static final ThreadLocal<String> ultimoEmpleadoEmail = new ThreadLocal<>();
    private static final ThreadLocal<String> ultimoEmpleadoPassword = new ThreadLocal<>();

    public static String getBaseUrl() {
        return System.getenv().getOrDefault("BASE_URL", "http://localhost:8085");
    }

    public static String getAuthUrl() {
        return System.getenv().getOrDefault("AUTH_URL", "http://localhost:8082");
    }

    public static String getAdminEmail() {
        return System.getenv().getOrDefault("ADMIN_EMAIL", "admin@empresa.com");
    }

    public static String getAdminPassword() {
        return System.getenv().getOrDefault("ADMIN_PASSWORD", "admin123");
    }

    public static String getToken() {
        return token.get();
    }

    public static void setToken(String tokenValue) {
        token.set(tokenValue);
    }

    public static void clearToken() {
        token.remove();
    }

    public static Response getLastResponse() {
        return lastResponse.get();
    }

    public static void setLastResponse(Response response) {
        lastResponse.set(response);
    }

    public static String getLastRequestBody() {
        return lastRequestBody.get();
    }

    public static void setLastRequestBody(String body) {
        lastRequestBody.set(body);
    }

    public static String getUltimoEmpleadoId() {
        return ultimoEmpleadoId.get();
    }

    public static void setUltimoEmpleadoId(String id) {
        ultimoEmpleadoId.set(id);
    }

    public static String getUltimoEmpleadoEmail() {
        return ultimoEmpleadoEmail.get();
    }

    public static void setUltimoEmpleadoEmail(String email) {
        ultimoEmpleadoEmail.set(email);
    }

    public static String getUltimoEmpleadoPassword() {
        return ultimoEmpleadoPassword.get();
    }

    public static void setUltimoEmpleadoPassword(String password) {
        ultimoEmpleadoPassword.set(password);
    }

    public static void clear() {
        token.remove();
        lastResponse.remove();
        ultimoEmpleadoId.remove();
        ultimoEmpleadoEmail.remove();
        ultimoEmpleadoPassword.remove();
    }
}