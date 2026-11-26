package intercard.logica;

import intercard.modelo.Persona;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Consulta los nombres y apellidos de una persona a partir de su DNI,
 * usando un servicio web externo (una API que responde en formato JSON).
 *
 * Ejemplo de respuesta del servicio cuando encuentra el DNI:
 * {"dni":"12345678","apellido_paterno":"PEREZ","apellido_materno":"GOMEZ","nombre":"JUAN", ...}
 *
 * Si no encuentra el DNI responde con el código HTTP 404.
 */
public class ConsultaDni {

    // Dirección del servicio. Al final se le agrega el DNI a consultar.
    private static final String URL_SERVICIO = "https://consulta.dniruc.top/api/personas/";

    // Tiempo máximo de espera para que el servicio responda
    private static final Duration TIEMPO_ESPERA = Duration.ofSeconds(10);

    private HttpClient clienteHttp;

    /**
     * Devuelve el cliente HTTP, creándolo la primera vez que se necesita.
     * Se crea aquí (y no en el constructor) para que, si falla, el error se
     * produzca durante la consulta y se pueda mostrar un mensaje al usuario.
     */
    private HttpClient obtenerClienteHttp() {
        if (clienteHttp == null) {
            clienteHttp = HttpClient.newBuilder()
                    .connectTimeout(TIEMPO_ESPERA)
                    .build();
        }
        return clienteHttp;
    }

    /**
     * Un DNI peruano tiene exactamente 8 dígitos.
     */
    public static boolean esDniValido(String dni) {
        return dni.matches("\\d{8}");
    }

    /**
     * Busca los datos de una persona por su DNI.
     *
     * @return una Persona con DNI, nombres y apellidos, o null si el servicio
     *         no tiene datos de ese DNI.
     * @throws IOException si no se pudo conectar con el servicio o respondió con error.
     * @throws InterruptedException si la consulta fue interrumpida.
     */
    public Persona consultar(String dni) throws IOException, InterruptedException {
        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(URL_SERVICIO + dni))
                .timeout(TIEMPO_ESPERA)
                .GET()
                .build();

        HttpResponse<String> respuesta = obtenerClienteHttp().send(solicitud, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() == 404) {
            return null; // El servicio no tiene datos de ese DNI
        }
        if (respuesta.statusCode() != 200) {
            throw new IOException("El servicio respondió con el código " + respuesta.statusCode());
        }

        String json = respuesta.body();
        String nombres = leerCampo(json, "nombre");
        String apellidoPaterno = leerCampo(json, "apellido_paterno");
        String apellidoMaterno = leerCampo(json, "apellido_materno");

        if (nombres == null) {
            return null;
        }
        if (apellidoPaterno == null) {
            apellidoPaterno = "";
        }
        if (apellidoMaterno == null) {
            apellidoMaterno = "";
        }
        String apellidos = (apellidoPaterno + " " + apellidoMaterno).trim();

        // Solo se guardan los datos que el sistema necesita
        // (no se usan la fecha de nacimiento ni el sexo que también envía el servicio).
        return new Persona(dni, nombres, apellidos, "");
    }

    /**
     * Lee el valor de texto de un campo en un JSON sencillo.
     * Ejemplo: en {"nombre":"JUAN"} el campo "nombre" vale "JUAN".
     * Devuelve null si el campo no existe.
     *
     * Nota: si más adelante se necesitan respuestas más complejas,
     * conviene usar una librería para JSON (por ejemplo Gson).
     */
    private String leerCampo(String json, String campo) {
        String clave = "\"" + campo + "\":\"";
        int inicio = json.indexOf(clave);
        if (inicio == -1) {
            return null;
        }
        inicio = inicio + clave.length();
        int fin = json.indexOf("\"", inicio);
        if (fin == -1) {
            return null;
        }
        return json.substring(inicio, fin);
    }
}
