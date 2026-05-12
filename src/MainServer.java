import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainServer {
    private static List<Thread> hilosActivos = new ArrayList<>();

    // CORRECCIÓN 3: Declaramos el buffer a nivel global para que la web lo pueda leer
    private static Buffer bufferActual;

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

        // CORRECCIÓN 1: Restauramos el endpoint de estado que faltaba
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

        // Recibir valores de sliders
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

    private static synchronized void reiniciarSimulacion(int numProds, int numConsum, int buffSize) {
        for (Thread thread : hilosActivos) {
            thread.interrupt();
        }
        hilosActivos.clear();

        // CORRECCIÓN 3: Asignamos a la variable global (sin poner la palabra "Buffer" al inicio)
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