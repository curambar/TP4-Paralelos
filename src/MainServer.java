import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase principal que levanta un servidor HTTP para la interfaz gráfica web
 * y gestiona el ciclo de vida de los hilos de la simulación concurrente.
 */
public class MainServer {
    /** Lista de hilos productores y consumidores actualmente en ejecución. */
    private static final List<Thread> hilosActivos = new ArrayList<>();

    /** Referencia global al buffer actual, accesible para los endpoints HTTP. */
    private static Buffer bufferActual;

    /**
     * Punto de entrada principal. Inicia la simulación por defecto y configura
     * los endpoints del servidor web para despachar archivos y manejar la API.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     * @throws IOException Si ocurre un error al iniciar el servidor en el puerto 8080.
     */
    public static void main(String[] args) throws IOException {
        reiniciarSimulacion(2, 2, 5);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Servir la página en HTML
        server.createContext("/", exchange -> {
            try {
                // CORRECCIÓN 2: Protegemos la lectura del HTML con un try-catch
                byte[] respuesta = Files.readAllBytes(Paths.get("src", "index.html"));
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, respuesta.length);
                OutputStream os = exchange.getResponseBody();
                os.write(respuesta);
                os.close();
            } catch (IOException e) {
                // Si el HTML no está en la ruta correcta, ahora te lo avisa sin hacer "Net Reset"
                String error =
                        "ERROR: No se encontro el archivo index.html. Debe estar en esta ruta exacta: " + Paths.get(
                                "index.html").toAbsolutePath();
                exchange.sendResponseHeaders(404, error.length());
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
                System.out.println(error);
            }
        });

        // Servir el archivo CSS
        server.createContext("/styles.css", exchange -> {
            try {
                byte[] respuesta = Files.readAllBytes(Paths.get("src", "styles.css"));
                exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
                exchange.sendResponseHeaders(200, respuesta.length);
                OutputStream os = exchange.getResponseBody();
                os.write(respuesta);
                os.close();
            } catch (IOException e) {
                exchange.sendResponseHeaders(404, -1);
            }
        });

        // Servir el archivo JavaScript
        server.createContext("/api.js", exchange -> {
            try {
                byte[] respuesta = Files.readAllBytes(Paths.get("src", "api.js"));
                exchange.getResponseHeaders().set("Content-Type", "application/javascript; charset=UTF-8");
                exchange.sendResponseHeaders(200, respuesta.length);
                OutputStream os = exchange.getResponseBody();
                os.write(respuesta);
                os.close();
            } catch (IOException e) {
                exchange.sendResponseHeaders(404, -1);
            }
        });

        // Endpoint de lectura de estado para el frontend
        server.createContext("/api/estado", exchange -> {
            if (bufferActual != null) {
                String jsonResponse = bufferActual.toJson();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            }
        });

        // Endpoint para recibir valores de configuración y reiniciar la simulación
        server.createContext("/api/config", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String[] params = query.split("&");
                int p = Integer.parseInt(params[0].split("=")[1]);
                int c = Integer.parseInt(params[1].split("=")[1]);
                int s = Integer.parseInt(params[2].split("=")[1]);

                reiniciarSimulacion(p, c, s);

                String respuesta = "OK";
                exchange.sendResponseHeaders(200, respuesta.length());
                OutputStream os = exchange.getResponseBody();
                os.write(respuesta.getBytes());
                os.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor iniciado en http://localhost:8080");
    }

    /**
     * Interrumpe los hilos en ejecución, limpia la lista de hilos activos y crea
     * una nueva instancia de la simulación con los parámetros indicados.
     * Es sincronizado para evitar condiciones de carrera durante la recarga.
     *
     * @param numProds Cantidad de hilos productores (Clientes).
     * @param numConsum Cantidad de hilos consumidores (Cocineros).
     * @param buffSize Capacidad máxima de la bandeja (Buffer).
     */
    private static synchronized void reiniciarSimulacion(int numProds, int numConsum, int buffSize) {
        for (Thread thread : hilosActivos) {
            thread.interrupt();
        }
        hilosActivos.clear();

        bufferActual = new Buffer(buffSize);

        // Iniciamos los hilos Productores (Ahora Clientes)
        for (int i = 0; i < numProds; i++) {
            Thread t = new Thread(new Productor(bufferActual), "Cliente-" + (i+1));
            hilosActivos.add(t);
            t.start();
        }

        // Iniciamos los hilos Consumidores (Ahora Cocineros)
        for (int i = 0; i < numConsum; i++) {
            Thread t = new Thread(new Consumidor(bufferActual), "Cocinero-" + (i+1));
            hilosActivos.add(t);
            t.start();
        }

        System.out.printf("Simulación reiniciada: %d productores | %d Consumidores | %d Bandeja%n", numProds,
                numConsum, buffSize);
    }
}