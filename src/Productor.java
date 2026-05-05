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
        while (true){
            try {
                Thread.sleep(random.nextInt(1000) + 500);
                buffer.producir(item++);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
