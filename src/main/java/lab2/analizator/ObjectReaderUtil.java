package lab2.analizator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

public class ObjectReaderUtil {

    @SuppressWarnings("unchecked")
    public static <T> Map<Integer, Map<String, T>> readMapFromFile(String filename) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Map<Integer, Map<String, T>>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

}
