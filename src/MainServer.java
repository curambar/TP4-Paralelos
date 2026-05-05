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

    public static void main(String[] args) throws IOException {
        reiniciarSimulacion(2, 2, 5);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Servir la página en HTML
        server.createContext("/", exchange -> {
            byte[] respuesta = Files.readAllBytes(Paths.get("index.html"));
            exchange.sendResponseHeaders(200, respuesta.length);
            OutputStream os = exchange.getResponseBody();
            os.write(respuesta);
            os.close();
        });

        // Recibir valores de sliders
        server.createContext("/api/config", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Leemos parámetros de la URL
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
     * Detiene los hilos actuales y crea nuevos con las cantidades provistas.
     * @param numProds el número de productores, entero positivo.
     * @param numConsum el número de productores, entero positivo.
     * @param buffSize el número de productores, entero positivo.
     */
    private static synchronized void reiniciarSimulacion(int numProds, int numConsum, int buffSize) {
        for(Thread thread : hilosActivos) {
            thread.interrupt();
        }
        hilosActivos.clear();

        Buffer bufferActual = new Buffer(buffSize);

        //Iniciamos los hilos productores
        for (int i = 0; i < numProds; i++) {
            Thread t = new Thread(new Productor(bufferActual), "Productor-" + i);
            hilosActivos.add(t);
            t.start();
        }

        for (int i = 0; i < numConsum; i++) {
            Thread t = new Thread(new Consumidor(bufferActual), "Consumidor-" + i);
            hilosActivos.add(t);
            t.start();
        }
        System.out.printf("Similación reiniciada: %d productores | %d Consumidores | %d Bandeja%n", numProds, numConsum, buffSize);
    }
}
