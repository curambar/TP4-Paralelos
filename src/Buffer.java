import java.util.concurrent.Semaphore;

/**
 * Representa una cola
 */
public class Buffer {
    /**
     * El tamaño del buffer. Entero positivo.
     */
    public static final int BUFFER_SIZE = 5;
    /**
     * La cola de producción.
     */
    private int[] buffer = new int[BUFFER_SIZE];
    /**
     *
     */
    private int idProducir = 0;
    private int idConsumir = 0;

    /**
     * Mutex binario. Controla el acceso a la cola.
     */
    private Semaphore mutex = new Semaphore(1);
    /**
     * Cuenta los espacios disponibles.
     */
    private Semaphore vacios = new Semaphore(BUFFER_SIZE);
    /**
     * Cuenta los espacios listos para ser leidos.
     */
    private Semaphore llenos = new Semaphore(0);

    /**
     * Intenta imprimir un string.
     * @param item la cadena a imprimir.
     * @param nombreHilo identifica el hilo.
     */
    public void producir(int item, String nombreHilo){
        try {
            //Poner mutex segundo asegura que no se bloquea si 'vacios' lo pone en espera.
            vacios.acquire();
            mutex.acquire();

            //Sección crítica.
            buffer[idProducir] = item;
            idProducir = (1+ idProducir) % BUFFER_SIZE; // incrementa el contador circular.
            log(nombreHilo, "PRODUCE " + item);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
            llenos.release();
        }
    }

    /**
     * Intenta consumir un hilo.
     * @param nombreHilo identifica el hilo.
     */
    public void consumir(String nombreHilo){
        try {
            //Una vez mas, el orden es crítico para no bloquear el mutex.
            llenos.acquire();
            mutex.acquire();

            // Sección crítica
            int item = buffer[idConsumir];
            idConsumir = (1 + idConsumir) % BUFFER_SIZE; // Incrementa el contador circular.
            log(nombreHilo, "CONSUME " + item);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
            vacios.release();
        }
    }

    private void log(String hilo, String accion){
        System.out.printf("[%s] %-20s | Permisos -> mutex: %d, vacíos: %d, llenos: %d%n",
                hilo, accion, mutex.availablePermits(), vacios.availablePermits(), llenos.availablePermits());
    }
}
