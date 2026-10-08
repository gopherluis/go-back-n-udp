package protocol;

public class ProtocolConfig {
    public static final int DEFAULT_RECEIVER_PORT = 5000;

    public static final int MAX_PAYLOAD_SIZE = 1024;
    public static final int HEADER_SIZE = 11;
    public static final int MAX_PACKET_SIZE = HEADER_SIZE + MAX_PAYLOAD_SIZE;

    public static final int TIMEOUT_MS = 500;

    private ProtocolConfig() {
        // Evita instanciar classe de configuração
    }
}