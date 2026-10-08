package protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {
    public static final int HEADER_SIZE = ProtocolConfig.HEADER_SIZE;
    public static final int MAX_PAYLOAD_SIZE = ProtocolConfig.MAX_PAYLOAD_SIZE;
    public static final int MAX_PACKET_SIZE = ProtocolConfig.MAX_PACKET_SIZE;

    private final PacketType type;
    private final int sequenceNumber;
    private final int ackNumber;
    private final short dataLength;
    private final byte[] data;

    public Packet(PacketType type, int sequenceNumber, int ackNumber, byte[] data) {
        this.type = type;
        this.sequenceNumber = sequenceNumber;
        this.ackNumber = ackNumber;

        if (data == null) {
            this.data = new byte[0];
        } else {
            if (data.length > MAX_PAYLOAD_SIZE) {
                throw new IllegalArgumentException("Payload maior que 1024 bytes.");
            }
            this.data = data;
        }

        this.dataLength = (short) this.data.length;
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + dataLength);

        buffer.put(type.getValue());
        buffer.putInt(sequenceNumber);
        buffer.putInt(ackNumber);
        buffer.putShort(dataLength);
        buffer.put(data);

        return buffer.array();
    }

    public static Packet fromBytes(byte[] bytes, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, length);

        PacketType type = PacketType.fromByte(buffer.get());
        int sequenceNumber = buffer.getInt();
        int ackNumber = buffer.getInt();
        short dataLength = buffer.getShort();

        byte[] data = new byte[dataLength];

        if (dataLength > 0) {
            buffer.get(data);
        }

        return new Packet(type, sequenceNumber, ackNumber, data);
    }

    public static Packet createDataPacket(int sequenceNumber, byte[] data) {
        return new Packet(PacketType.DATA, sequenceNumber, -1, data);
    }

    public static Packet createAckPacket(int ackNumber) {
        return new Packet(PacketType.ACK, -1, ackNumber, new byte[0]);
    }

    public static Packet createHandshakePacket(byte[] data) {
        return new Packet(PacketType.HANDSHAKE, -1, -1, data);
    }

    public static Packet createFinPacket() {
        return new Packet(PacketType.FIN, -1, -1, new byte[0]);
    }

    public PacketType getType() {
        return type;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getAckNumber() {
        return ackNumber;
    }

    public short getDataLength() {
        return dataLength;
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}