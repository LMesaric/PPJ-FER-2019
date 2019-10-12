package analizator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LA {

    public static void main(String[] args) {
        String inputText = new String(readAllFromStdin(), StandardCharsets.UTF_8);


    }

    private static byte[] readAllFromStdin() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[32 * 1024];

            int bytesRead;
            while ((bytesRead = System.in.read(buffer)) > 0) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
