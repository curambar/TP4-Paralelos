public class Main {

    public static void main(String[] args) {

        int numProductores = 2;
        int numConsumidores = 2;

        try {
            if (args.length == 1) {
                int num = Integer.parseInt(args[0]);
                if (num > 0) {
                    numProductores = num;
                    numConsumidores = num;
                }
            } else if (args.length == 2) {
                int num1 = Integer.parseInt(args[0]);
                int num2 = Integer.parseInt(args[1]);
                if (num1 > 0 && num2 > 0) {
                    numProductores = num1;
                    numConsumidores = num2;
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: Los argumentos deben ser números enteros válidos. Usando valores por defecto.");
        }

        System.out.printf("--- Iniciando Simulación: %d Productores | %d Consumidores ---%n",
                numProductores, numConsumidores);

        // Recurso compartido
        Buffer buffer = new Buffer();

        //Creamos y arrancamos productores
        Thread[] productores = new Thread[numProductores];
        for (int i = 0; i < numProductores; i++) {
            productores[i] = new Thread(new Productor(buffer), "Productor" + i);
            productores[i].start();
        }

        //Creamos y arrancamos consumidores
        Thread[] consumidores = new Thread[numConsumidores];
        for (int i = 0; i < numConsumidores; i++) {
            consumidores[i] = new Thread(new Consumidor(buffer), "Consumidor" + i);
            consumidores[i].start();
        }
    }


}
