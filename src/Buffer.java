import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Representa una cola de producción.
 */
public class Buffer {

    private int size;
    private int[] buffer;
    private int in = 0;
    private int out = 0;
    private Semaphore mutex;
    private Semaphore vacios;
    private Semaphore llenos;
    // Mapa concurrente
    private Map<String, String> estadoHilos = new ConcurrentHashMap<>();


    /**
     * Crea una cola del tamaño especificado.
     *
     * @param size el tamaño, entero positivo.
     */
    public Buffer(int size) {
        this.size = size;
        this.buffer = new int[size];
        this.mutex = new Semaphore(1);
        this.vacios = new Semaphore(size);
        this.llenos = new Semaphore(0);
    }

    public void setEstado(String nombreHilo, String estado){
        estadoHilos.put(nombreHilo, estado);
    }

    /**
     * Intenta producir un item en la cola.
     *
     * @param item       el objeto a producir.
     * @throws InterruptedException si el hilo es interrumpido.
     */
    public void producir(int item, String nombre) throws InterruptedException {
        setEstado(nombre, "ESPERANDO");
        vacios.acquire();
        mutex.acquire();
        try {
            setEstado(nombre, "SECCION_CRITICA");
            buffer[in] = item;
            in = (1 + in) % size; // incrementa el contador circular.
            Thread.sleep(200); // Para que se alcance a ver el cambio de color
        } finally {
            mutex.release();
            llenos.release();
        }
    }

    /**
     * Intenta consumir un hilo.
     *
     * @throws InterruptedException si el hilo es interrumpido.
     */
    public void consumir(String nombre) throws InterruptedException {
        setEstado(nombre, "ESPERANDO");
        llenos.acquire();
        mutex.acquire();
        try {
            setEstado(nombre, "SECCION_CRITICA");
            buffer[out] = 0;        // Para que se vea visualmente que está vacío.
            out = (1 + out) % size; // Incrementa el contador circular.
            Thread.sleep(200);
        } finally {
            mutex.release();
            vacios.release();
        }
    }

    /**
     * Recopila información del estado del Buffer y sus semáforos en formato JSON.
     * Adquiere el mutex antes de leer, asegurando lectura atómica.
     *
     * @return un JSON que representa el estado del sistema.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        try {
            mutex.acquire(); // Con esto bloqueamos productores y consumidores.

            sb.append("{");

            // Buffer
            sb.append("\"buffer\": [");
            for (int i = 0; i < size; i++) {
                sb.append(buffer[i]);
                if (i < size - 1) sb.append(", ");
            }
            sb.append("], ");
            sb.append("\"in\": ").append(in).append(", ");
            sb.append("\"out\": ").append(out).append(", ");

            // Semaforos
            sb.append("\"mutex\": ").append(mutex.availablePermits()).append(", ");
            sb.append("\"vacios\": ").append(vacios.availablePermits()).append(", ");
            sb.append("\"llenos\": ").append(llenos.availablePermits()).append(", ");

            // Productores
            sb.append("\"productores\": [");
            boolean primerP = true;
            for (Map.Entry<String, String> entry : estadoHilos.entrySet()) {
                if(entry.getKey().startsWith("Cliente")){
                    if (!primerP) sb.append(", ");
                    sb.append("{\"id\": \"").append(entry.getKey()).append("\", \"estado\": \"").append(entry.getValue()).append("\"}");
                    primerP = false;
                }
            }
            sb.append("], ");

            // Consumidores
            sb.append("\"consumidores\": [");
            boolean primerC = true;
            for (Map.Entry<String, String> entry : estadoHilos.entrySet()) {
                if(entry.getKey().startsWith("Cocinero")){
                    if (!primerC) sb.append(", ");
                    sb.append("{\"id\": \"").append(entry.getKey()).append("\", \"estado\": \"").append(entry.getValue()).append("\"}");
                    primerC = false;
                }
            }
            sb.append("]");

            sb.append("}");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "{}";
        } finally {
            mutex.release();
        }
        return sb.toString();
    }
}
