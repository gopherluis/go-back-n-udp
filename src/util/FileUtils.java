package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public static byte[] readFile(String filePath) throws IOException {
        return Files.readAllBytes(Path.of(filePath));
    }

    public static void writeFile(String filePath, byte[] data) throws IOException {
        File file = new File(filePath);

        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        Files.write(Path.of(filePath), data);
    }

    public static long getFileSize(String filePath) throws IOException {
        return Files.size(Path.of(filePath));
    }
}