import java.util.Random;

/**
 *
 */
public class Productor implements Runnable{
    private Buffer buffer;
    private Random random = new Random();

    public Productor(Buffer buffer){
        this.buffer = buffer;
    }

    /**
     *
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
