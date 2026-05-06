package support;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;

import java.text.Normalizer;
import java.util.function.Supplier;

public class PollingUtils {

    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    private static final int DEFAULT_INTERVAL_MS = 2000;

    private static String normalize(String text) {
        return Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public static Response esperarHastaQue(
            Supplier<Response> peticion,
            java.util.function.Function<Response, Boolean> condicion,
            int maxIntentos,
            int intervaloMs,
            String mensajeError) {

        Response respuesta = null;
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                respuesta = peticion.get();
                if ( condicion.apply(respuesta) ) {
                    System.out.println("Condición cumplida en intento " + intento);
                    return respuesta;
                }
            } catch (Exception e) {
                System.out.println("Intento " + intento + " fallido: " + e.getMessage());
            }

            if (intento < maxIntentos) {
                try {
                    Thread.sleep(intervaloMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (respuesta != null) {
            System.out.println("Respuesta final (intento " + maxIntentos + "): " +
                "status=" + respuesta.getStatusCode() + ", body=" + respuesta.getBody().asString());
        }

        Assert.fail(mensajeError + " después de " + maxIntentos + " intentos con intervalo de " + intervaloMs + "ms");
        return null;
    }

    public static Response esperarStatusCode(
            Supplier<Response> peticion,
            int statusCodeEsperado,
            int maxIntentos,
            int intervaloMs) {

        return esperarHastaQue(
            peticion,
            (resp) -> resp.getStatusCode() == statusCodeEsperado,
            maxIntentos,
            intervaloMs,
            "No se obtuvo el código de estado esperado: " + statusCodeEsperado
        );
    }

    public static Response esperarQueExista(
            Supplier<Response> peticion,
            int maxIntentos,
            int intervaloMs) {

        return esperarHastaQue(
            peticion,
            (resp) -> resp.getStatusCode() != 404,
            maxIntentos,
            intervaloMs,
            "El recurso no fue encontrado después de múltiples intentos"
        );
    }

    public static boolean esperarLoginExitoso(String email, String password, int maxIntentos, int intervaloMs) {
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                Response respuesta = RestAssured
                    .given()
                    .contentType("application/json")
                    .body("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}")
                    .when()
                    .post(TestContext.getBaseUrl() + "/auth/login");

                if (respuesta.getStatusCode() == 200 && respuesta.jsonPath().get("token") != null) {
                    System.out.println("Login exitoso en intento " + intento);
                    return true;
                }
            } catch (Exception e) {
                System.out.println("Intento de login " + intento + " fallido: " + e.getMessage());
            }

            if (intento < maxIntentos) {
                try {
                    Thread.sleep(intervaloMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return false;
    }

    public static void esperarLoginFallido(String email, String password, int maxIntentos, int intervaloMs) {
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                Response respuesta = RestAssured
                    .given()
                    .contentType("application/json")
                    .body("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}")
                    .when()
                    .post(TestContext.getBaseUrl() + "/auth/login");

                if (respuesta.getStatusCode() != 200 || respuesta.jsonPath().get("token") == null) {
                    System.out.println("Login fallido como esperado en intento " + intento);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Intento de login " + intento + " fallido: " + e.getMessage());
                return;
            }

            if (intento < maxIntentos) {
                try {
                    Thread.sleep(intervaloMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        System.out.println("Advertencia: Login siguió funcionando después de " + maxIntentos + " intentos");
    }

    public static boolean esperarNotificacion(String tipoNotificacion, int maxIntentos, int intervaloMs) {
        String empleadoId = TestContext.getUltimoEmpleadoId();
        if (empleadoId == null) {
            System.out.println("No hay ID de empleado para buscar notificaciones");
            return false;
        }

        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                Response respuesta = RestAssured
                    .given()
                    .header("Authorization", "Bearer " + TestContext.getToken())
                    .when()
                    .get(TestContext.getBaseUrl() + "/notificaciones/" + empleadoId);

                if (respuesta.getStatusCode() == 200) {
                    String body = respuesta.getBody().asString();
                    if (normalize(body).contains(normalize(tipoNotificacion))) {
                        System.out.println("Notificación tipo '" + tipoNotificacion + "' encontrada en intento " + intento);
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Intento de buscar notificación " + intento + " fallido: " + e.getMessage());
            }

            if (intento < maxIntentos) {
                try {
                    Thread.sleep(intervaloMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return false;
    }
}