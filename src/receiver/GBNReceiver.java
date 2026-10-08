package receiver;

import protocol.Packet;
import protocol.PacketType;
import protocol.ProtocolConfig;
import util.FileUtils;
import util.HashUtils;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class GBNReceiver {

    private final ReceiverStats stats = new ReceiverStats();

    public void start() throws Exception {
        try (DatagramSocket socket = new DatagramSocket(ProtocolConfig.DEFAULT_RECEIVER_PORT)) {
            System.out.println("Aguardando handshake na porta " + ProtocolConfig.DEFAULT_RECEIVER_PORT + "...");

            DatagramPacket handshakeDatagram = receiveDatagram(socket);
            Packet handshakePacket = Packet.fromBytes(
                    handshakeDatagram.getData(),
                    handshakeDatagram.getLength()
            );

            if (handshakePacket.getType() != PacketType.HANDSHAKE) {
                throw new IllegalStateException("Pacote inesperado. Esperado: HANDSHAKE.");
            }

            String handshakeData = new String(handshakePacket.getData());

            System.out.println("Handshake recebido de " + handshakeDatagram.getAddress().getHostAddress());
            System.out.println("Dados da sessão: " + handshakeData);

            sendAck(socket, handshakeDatagram, 0);
            System.out.println("ACK do handshake enviado.");

            String[] parts = handshakeData.split(";", 4);

            double lossProbability = Double.parseDouble(parts[0]);
            String destinationPath = parts[1];
            long fileSize = Long.parseLong(parts[2]);
            String originalHash = parts[3];

            System.out.println("Tamanho do arquivo: " + fileSize + " bytes");
            System.out.println("MD5 original: " + originalHash);

            LossSimulator lossSimulator = new LossSimulator(lossProbability);

            receiveFile(socket, destinationPath, lossSimulator, originalHash);
        }
    }

    private void receiveFile(
            DatagramSocket socket,
            String destinationPath,
            LossSimulator lossSimulator,
            String originalHash
    ) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int expectedSeqNum = 0;

        while (true) {
            DatagramPacket dataDatagram = receiveDatagram(socket);

            Packet packet = Packet.fromBytes(
                    dataDatagram.getData(),
                    dataDatagram.getLength()
            );

            if (packet.getType() == PacketType.DATA) {
                stats.incrementPacketsReceived();

                int seq = packet.getSequenceNumber();

                if (seq == expectedSeqNum) {
                    if (lossSimulator.shouldDrop()) {
                        stats.incrementArtificialLosses();
                        System.out.println("Pacote perdido artificialmente. Seq = " + seq);
                        continue;
                    }

                    outputStream.write(packet.getData());
                    stats.addBytesReceived(packet.getDataLength());

                    System.out.println("Pacote DATA recebido em ordem. Seq = " + seq);

                    sendAck(socket, dataDatagram, seq);
                    System.out.println("ACK enviado. Ack = " + seq);

                    expectedSeqNum++;
                } else {
                    int lastAck = expectedSeqNum - 1;

                    sendAck(socket, dataDatagram, lastAck);
                    stats.incrementOutOfOrderPackets();

                    System.out.println("Pacote fora de ordem descartado. Seq = " + seq);
                    System.out.println("ACK duplicado reenviado. Ack = " + lastAck);
                }
            } else if (packet.getType() == PacketType.FIN) {
                FileUtils.writeFile(destinationPath, outputStream.toByteArray());

                String receivedHash = HashUtils.calculateMD5(destinationPath);

                System.out.println("MD5 recebido: " + receivedHash);

                if (receivedHash.equalsIgnoreCase(originalHash)) {
                    System.out.println("Integridade verificada: arquivos idênticos.");
                } else {
                    System.out.println("ERRO: arquivo recebido diferente do original.");
                }

                System.out.println("FIN recebido.");
                System.out.println("Arquivo salvo em: " + destinationPath);
                System.out.println("Transferência encerrada.");

                stats.printStats();

                break;
            }
        }
    }

    private DatagramPacket receiveDatagram(DatagramSocket socket) throws Exception {
        byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);

        socket.receive(datagram);

        return datagram;
    }

    private void sendAck(DatagramSocket socket, DatagramPacket receivedDatagram, int ackNumber) throws Exception {
        Packet ackPacket = Packet.createAckPacket(ackNumber);
        byte[] ackBytes = ackPacket.toBytes();

        DatagramPacket ackDatagram = new DatagramPacket(
                ackBytes,
                ackBytes.length,
                receivedDatagram.getAddress(),
                receivedDatagram.getPort()
        );

        socket.send(ackDatagram);
        stats.incrementAcksSent();
    }
}