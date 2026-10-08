package receiver;

public class ReceiverStats {

    private int packetsReceived;
    private int artificialLosses;
    private int outOfOrderPackets;
    private int acksSent;
    private long bytesReceived;

    public void incrementPacketsReceived() {
        packetsReceived++;
    }

    public void incrementArtificialLosses() {
        artificialLosses++;
    }

    public void incrementOutOfOrderPackets() {
        outOfOrderPackets++;
    }

    public void incrementAcksSent() {
        acksSent++;
    }

    public void addBytesReceived(int bytes) {
        bytesReceived += bytes;
    }

    public void printStats() {
        double effectiveLossRate = packetsReceived == 0
                ? 0.0
                : ((double) artificialLosses / packetsReceived) * 100;

        System.out.println();
        System.out.println("===== ESTATÍSTICAS DO RECEPTOR =====");
        System.out.println("Pacotes DATA recebidos........: " + packetsReceived);
        System.out.println("Perdas artificiais............: " + artificialLosses);
        System.out.println("Pacotes fora de ordem.........: " + outOfOrderPackets);
        System.out.println("ACKs enviados.................: " + acksSent);
        System.out.println("Bytes recebidos...............: " + bytesReceived);
        System.out.printf("Taxa de perda efetiva.........: %.2f%%\n", effectiveLossRate);
    }
}