import java.util.concurrent.Semaphore;

/**
 * Representa una cola de producción.
 */
public class Buffer {

    /**
     * El tamaño del buffer. Entero positivo.
     */
    private int size;

    /**
     * La cola de producción.
     */
    private int[] buffer;

    /**
     * Índice de producción.
     */
    private int in = 0;

    /**
     * Índice de consumo.
     */
    private int out = 0;

    /**
     * Mutex binario. Controla el acceso a la cola.
     */
    private Semaphore mutex;

    /**
     * Cuenta los espacios disponibles.
     */
    private Semaphore vacios;

    /**
     * Cuenta los espacios listos para ser leidos.
     */
    private Semaphore llenos;

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

    /**
     * Intenta producir un item en la cola.
     *
     * @param item       el objeto a producir.
     * @throws InterruptedException si el hilo es interrumpido.
     */
    public void producir(int item) throws InterruptedException {
        vacios.acquire();
        mutex.acquire();
        try {
            buffer[in] = item;
            in = (1 + in) % size; // incrementa el contador circular.
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
    public void consumir() throws InterruptedException {
        llenos.acquire();
        mutex.acquire();
        try {
            buffer[out] = 0;        // Para que se vea visualmente que está vacío.
            out = (1 + out) % size; // Incrementa el contador circular.
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
            sb.append("\"buffer\": [");
            for (int i = 0; i < size; i++) {
                sb.append(buffer[i]);
                if (i < size - 1) sb.append(", ");
            }
            sb.append("], ");
            sb.append("\"in\": ").append(in).append(", ");
            sb.append("\"out\": ").append(out).append(", ");
            sb.append("\"mutex\": ").append(mutex.availablePermits()).append(", ");
            sb.append("\"vacios\": ").append(vacios.availablePermits()).append(", ");
            sb.append("\"llenos\": ").append(llenos.availablePermits());
            sb.append("}");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        return sb.toString();
    }
}
