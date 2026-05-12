import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Representa una cola de producción (Bandeja de pedidos) circular.
 * Gestiona el acceso concurrente mediante semáforos para garantizar
 * la sincronización entre productores y consumidores.
 */
public class Buffer {

    /** Capacidad máxima del buffer circular. */
    private final int size;
    /** Arreglo interno que almacena los pedidos. */
    private final int[] buffer;
    /** Puntero circular para la próxima inserción. */
    private int in = 0;
    /** Puntero circular para la próxima extracción. */
    private int out = 0;
    /** Semáforo para asegurar la exclusión mutua en la sección crítica. */
    private final Semaphore mutex;
    /** Semáforo contador de espacios disponibles en la bandeja. */
    private final Semaphore vacios;
    /** Semáforo contador de pedidos listos para ser procesados. */
    private final Semaphore llenos;

    /** Mapa concurrente que almacena el estado actual de cada hilo para la interfaz. */
    private final Map<String, String> estadoHilos = new ConcurrentHashMap<>();


    /**
     * Crea una cola del tamaño especificado e inicializa los semáforos correspondientes.
     *
     * @param size El tamaño de la cola, debe ser un entero positivo.
     */
    public Buffer(int size) {
        this.size = size;
        this.buffer = new int[size];
        this.mutex = new Semaphore(1);
        this.vacios = new Semaphore(size);
        this.llenos = new Semaphore(0);
    }

    /**
     * Actualiza el estado visual de un hilo en el mapa concurrente.
     *
     * @param nombreHilo Identificador del hilo (ej. Cliente-1, Cocinero-2).
     * @param estado Etiqueta representativa del estado actual del hilo.
     */
    public void setEstado(String nombreHilo, String estado){
        estadoHilos.put(nombreHilo, estado);
    }

    /**
     * Intenta insertar un pedido (item) en el buffer.
     * Se bloquea si no hay espacios vacíos o si la sección crítica está ocupada.
     *
     * @param item El número de pedido a producir.
     * @param nombre El identificador del hilo productor que realiza la acción.
     * @throws InterruptedException Si el hilo es interrumpido durante la espera.
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
     * Intenta retirar y procesar un pedido del buffer.
     * Se bloquea si no hay elementos disponibles o si la sección crítica está ocupada.
     *
     * @param nombre El identificador del hilo consumidor que realiza la acción.
     * @throws InterruptedException Si el hilo es interrumpido durante la espera.
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
     * Recopila información en tiempo real del estado del Buffer, semáforos
     * y los estados de los hilos en formato JSON.
     * Adquiere el mutex antes de leer para asegurar una lectura atómica consistente.
     *
     * @return Una cadena en formato JSON representando el estado global del sistema.
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

            // Semáforos
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
