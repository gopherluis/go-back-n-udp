package protocol;

public enum PacketType {
    DATA(0),
    ACK(1),
    HANDSHAKE(2),
    FIN(3);

    private final byte value;

    PacketType(int value) {
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }

    public static PacketType fromByte(byte value) {
        for (PacketType type : values()) {
            if (type.value == value) {
                return type;
            }
        }

        throw new IllegalArgumentException("Tipo de pacote inválido: " + value);
    }
}