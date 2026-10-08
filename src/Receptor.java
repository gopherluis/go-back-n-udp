import receiver.GBNReceiver;

public class Receptor {

    public static void main(String[] args) {
        try {
            System.out.println("=== RECEPTOR GBN ===");

            GBNReceiver receiver = new GBNReceiver();
            receiver.start();

        } catch (Exception e) {
            System.err.println("Erro no receptor: " + e.getMessage());
        }
    }
}