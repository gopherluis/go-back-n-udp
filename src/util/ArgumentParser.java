package util;

public class ArgumentParser {

    public static SenderArguments parseSenderArguments(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Uso correto: java Emissor <arquivo_origem> <IP_destino>:<path_destino> <tamanho_janela> <prob_perda>"
            );
        }

        String sourceFilePath = args[0];

        String destinationInfo = args[1];
        int separatorIndex = destinationInfo.indexOf(":");

        if (separatorIndex == -1) {
            throw new IllegalArgumentException(
                    "Destino inválido. Use o formato <IP_destino>:<path_destino>"
            );
        }

        String destinationIp = destinationInfo.substring(0, separatorIndex);
        String destinationPath = destinationInfo.substring(separatorIndex + 1);

        int windowSize;
        try {
            windowSize = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tamanho da janela deve ser um número inteiro.");
        }

        if (windowSize <= 0) {
            throw new IllegalArgumentException("Tamanho da janela deve ser maior que zero.");
        }

        double lossProbability;
        try {
            lossProbability = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Probabilidade de perda deve ser um número real.");
        }

        if (lossProbability < 0.0 || lossProbability > 1.0) {
            throw new IllegalArgumentException("Probabilidade de perda deve estar entre 0.0 e 1.0.");
        }

        return new SenderArguments(
                sourceFilePath,
                destinationIp,
                destinationPath,
                windowSize,
                lossProbability
        );
    }

    public static class SenderArguments {
        private final String sourceFilePath;
        private final String destinationIp;
        private final String destinationPath;
        private final int windowSize;
        private final double lossProbability;

        public SenderArguments(
                String sourceFilePath,
                String destinationIp,
                String destinationPath,
                int windowSize,
                double lossProbability
        ) {
            this.sourceFilePath = sourceFilePath;
            this.destinationIp = destinationIp;
            this.destinationPath = destinationPath;
            this.windowSize = windowSize;
            this.lossProbability = lossProbability;
        }

        public String getSourceFilePath() {
            return sourceFilePath;
        }

        public String getDestinationIp() {
            return destinationIp;
        }

        public String getDestinationPath() {
            return destinationPath;
        }

        public int getWindowSize() {
            return windowSize;
        }

        public double getLossProbability() {
            return lossProbability;
        }
    }
}