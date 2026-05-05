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
        while(true){
            try {
                Thread.sleep(random.nextInt(1500) + 500);
                buffer.consumir(Thread.currentThread().getName());
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " interrumpido.");
                Thread.currentThread().interrupt();
                break;
            }
        }

    }
}
