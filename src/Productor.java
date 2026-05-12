import java.util.Random;

/**
 * Representa un hilo productor (Cliente).
 * Simula el comportamiento de creación de pedidos de manera concurrente
 * para insertarlos en el buffer compartido.
 */
public class Productor implements Runnable{

    /** Referencia al buffer compartido en el que se insertarán elementos. */
    private final Buffer buffer;
    /** Generador de tiempos aleatorios para simular el tiempo de creación del pedido. */
    private final Random random = new Random();

    /**
     * Inicializa un nuevo hilo productor.
     *
     * @param buffer La instancia del buffer compartido.
     */
    public Productor(Buffer buffer){
        this.buffer = buffer;
    }

    /**
     * Ciclo de vida principal del hilo.
     * Alterna entre un estado de trabajo (simulado por un retraso aleatorio) y la
     * intención de depositar un número de pedido incremental en la bandeja compartida.
     */
    @Override
    public void run() {
        int item = 1;
        String nombre = Thread.currentThread().getName();

        while (true){
            try {
                buffer.setEstado(nombre, "TRABAJANDO");
                //noinspection BusyWait
                Thread.sleep(random.nextInt(1500) + 500);
                buffer.producir(item++, nombre);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
