package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static String calculateMD5(String filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            FileInputStream inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[4096];

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            inputStream.close();

            byte[] hashBytes = digest.digest();
            StringBuilder hashHex = new StringBuilder();

            for (byte b : hashBytes) {
                hashHex.append(String.format("%02x", b));
            }

            return hashHex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo MD5 não disponível.", e);
        }
    }
}