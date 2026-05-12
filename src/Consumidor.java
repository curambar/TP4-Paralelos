import java.util.Random;

/**
 * Representa un hilo consumidor (Cocinero).
 * Simula el comportamiento de procesar y retirar pedidos del buffer compartido.
 */
public class Consumidor implements Runnable{

    /** Referencia al buffer compartido del que se consumirán elementos. */
    private final Buffer buffer;
    /** Generador de tiempos aleatorios para simular el tiempo de cocinado. */
    private final Random random = new Random();

    /**
     * Inicializa un nuevo hilo consumidor.
     *
     * @param buffer La instancia del buffer compartido.
     */
    public Consumidor(Buffer buffer){
        this.buffer = buffer;
    }

    /**
     * Ciclo de vida principal del hilo.
     * Alterna entre un estado de trabajo (simulando tiempo de procesamiento) y
     * la extracción de un elemento del buffer. Si el hilo es interrumpido, finaliza la ejecución.
     */
    @Override
    public void run() {
        String nombre = Thread.currentThread().getName();
        while(true){
            try {
                buffer.setEstado(nombre, "TRABAJANDO");
                //noinspection BusyWait
                Thread.sleep(random.nextInt(2000) + 500);
                buffer.consumir(nombre);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

    }
}
