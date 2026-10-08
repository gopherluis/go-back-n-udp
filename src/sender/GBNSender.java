package sender;

import protocol.Packet;
import protocol.PacketType;
import protocol.ProtocolConfig;
import util.ArgumentParser.SenderArguments;
import util.FileUtils;
import util.HashUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class GBNSender {

    private final SenderArguments senderArgs;
    private final SenderStats stats = new SenderStats();

    private int base;
    private int nextSeqNum;
    private int totalPackets;
    private boolean transmissionFinished;

    public GBNSender(SenderArguments senderArgs) {
        this.senderArgs = senderArgs;
    }

    public void start() throws Exception {
        InetAddress receiverAddress = InetAddress.getByName(senderArgs.getDestinationIp());

        try (DatagramSocket socket = new DatagramSocket()) {
            stats.startTimer();

            sendHandshake(socket, receiverAddress);
            sendFileGoBackN(socket, receiverAddress);
            sendFin(socket, receiverAddress);

            String originalHash = HashUtils.calculateMD5(senderArgs.getSourceFilePath());
            System.out.println("MD5 do arquivo original: " + originalHash);

            stats.stopTimer();
            stats.printStats();
        }
    }

    private void sendHandshake(DatagramSocket socket, InetAddress receiverAddress) throws Exception {
        long fileSize = FileUtils.getFileSize(senderArgs.getSourceFilePath());
        String originalHash = HashUtils.calculateMD5(senderArgs.getSourceFilePath());

        String handshakeMessage = senderArgs.getLossProbability() + ";" +
                senderArgs.getDestinationPath() + ";" +
                fileSize + ";" +
                originalHash;

        Packet handshakePacket = Packet.createHandshakePacket(handshakeMessage.getBytes());
        sendPacket(socket, receiverAddress, handshakePacket);

        System.out.println("Handshake enviado. Aguardando ACK...");

        byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
        DatagramPacket ackDatagram = new DatagramPacket(buffer, buffer.length);

        socket.receive(ackDatagram);

        Packet ackPacket = Packet.fromBytes(ackDatagram.getData(), ackDatagram.getLength());

        if (ackPacket.getType() == PacketType.ACK) {
            System.out.println("ACK do handshake recebido.");
        } else {
            throw new IllegalStateException("Resposta inesperada do receptor.");
        }
    }

    private void sendFileGoBackN(DatagramSocket socket, InetAddress receiverAddress) throws Exception {
        byte[] fileData = FileUtils.readFile(senderArgs.getSourceFilePath());
        stats.setWindowSize(senderArgs.getWindowSize());

        totalPackets = (int) Math.ceil((double) fileData.length / Packet.MAX_PAYLOAD_SIZE);

        Packet[] packets = new Packet[totalPackets];

        for (int i = 0; i < totalPackets; i++) {
            int offset = i * Packet.MAX_PAYLOAD_SIZE;
            int chunkSize = Math.min(Packet.MAX_PAYLOAD_SIZE, fileData.length - offset);

            byte[] chunk = new byte[chunkSize];
            System.arraycopy(fileData, offset, chunk, 0, chunkSize);

            packets[i] = Packet.createDataPacket(i, chunk);
        }

        base = 0;
        nextSeqNum = 0;
        transmissionFinished = false;

        Thread ackThread = new Thread(() -> receiveAcks(socket));
        ackThread.start();

        while (base < totalPackets) {

            synchronized (this) {
                while (nextSeqNum < base + senderArgs.getWindowSize() && nextSeqNum < totalPackets) {
                    sendPacket(socket, receiverAddress, packets[nextSeqNum]);

                    stats.incrementPacketsSent();
                    stats.addBytesSent(packets[nextSeqNum].getDataLength());

                    System.out.println("Pacote DATA enviado. Seq = " + nextSeqNum);

                    nextSeqNum++;
                }
            }

            Thread.sleep(20);

            synchronized (this) {
                if (base < nextSeqNum) {
                    long timeoutStart = System.currentTimeMillis();

                    while (base < nextSeqNum &&
                            System.currentTimeMillis() - timeoutStart < ProtocolConfig.TIMEOUT_MS) {
                        wait(20);
                    }

                    if (base < nextSeqNum &&
                            System.currentTimeMillis() - timeoutStart >= ProtocolConfig.TIMEOUT_MS) {

                        System.out.println("TIMEOUT! Retransmitindo de " + base + " até " + (nextSeqNum - 1));

                        for (int i = base; i < nextSeqNum; i++) {
                            sendPacket(socket, receiverAddress, packets[i]);

                            stats.incrementPacketsSent();
                            stats.incrementRetransmissions();
                            stats.addBytesSent(packets[i].getDataLength());

                            System.out.println("Pacote retransmitido. Seq = " + i);
                        }
                    }
                }
            }
        }

        transmissionFinished = true;
        ackThread.join();
    }

    private void receiveAcks(DatagramSocket socket) {
        try {
            socket.setSoTimeout(100);

            while (!transmissionFinished) {
                try {
                    byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
                    DatagramPacket ackDatagram = new DatagramPacket(buffer, buffer.length);

                    socket.receive(ackDatagram);

                    Packet ackPacket = Packet.fromBytes(
                            ackDatagram.getData(),
                            ackDatagram.getLength()
                    );

                    if (ackPacket.getType() == PacketType.ACK) {
                        int ackNumber = ackPacket.getAckNumber();

                        synchronized (this) {
                            System.out.println("ACK recebido. Ack = " + ackNumber);
                            stats.incrementAcksReceived();

                            if (ackNumber >= base) {
                                base = ackNumber + 1;
                                notifyAll();
                            }
                        }
                    }

                } catch (SocketTimeoutException ignored) {
                    // Timeout curto apenas para a thread verificar se a transmissão terminou.
                }
            }

            socket.setSoTimeout(0);

        } catch (Exception e) {
            System.err.println("Erro na thread de ACKs: " + e.getMessage());
        }
    }

    private void sendPacket(DatagramSocket socket, InetAddress receiverAddress, Packet packet) throws Exception {
        byte[] packetBytes = packet.toBytes();

        DatagramPacket datagram = new DatagramPacket(
                packetBytes,
                packetBytes.length,
                receiverAddress,
                ProtocolConfig.DEFAULT_RECEIVER_PORT
        );

        socket.send(datagram);
    }

    private void sendFin(DatagramSocket socket, InetAddress receiverAddress) throws Exception {
        Packet finPacket = Packet.createFinPacket();

        sendPacket(socket, receiverAddress, finPacket);

        System.out.println("Pacote FIN enviado.");
        System.out.println("Transmissão finalizada pelo emissor.");
    }
}