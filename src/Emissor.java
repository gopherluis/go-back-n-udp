import sender.GBNSender;
import util.ArgumentParser;
import util.ArgumentParser.SenderArguments;

public class Emissor {

    public static void main(String[] args) {
        try {
            SenderArguments senderArgs = ArgumentParser.parseSenderArguments(args);

            System.out.println("=== EMISSOR GBN ===");

            GBNSender sender = new GBNSender(senderArgs);
            sender.start();

        } catch (Exception e) {
            System.err.println("Erro no emissor: " + e.getMessage());
        }
    }
}