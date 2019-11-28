package lab2.analizator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

public class ObjectReaderUtil {

    @SuppressWarnings("unchecked")
    public static Map<Integer, Map<String, Object>> readMapFromFile(String filename) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Map<Integer, Map<String, Object>>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

}
