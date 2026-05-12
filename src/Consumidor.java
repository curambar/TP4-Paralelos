import java.util.Random;

public class Consumidor implements Runnable{
    private Buffer buffer;
    private Random random = new Random();

    public Consumidor(Buffer buffer){
        this.buffer = buffer;
    }

    /**
     *
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
