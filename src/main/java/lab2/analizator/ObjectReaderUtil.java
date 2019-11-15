package lab2.analizator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;

public class ObjectReaderUtil {

    @SuppressWarnings("unchecked")
    public static Map<String, Set<String>> readMapFromFile(String filename) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Map<String, Set<String>>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

}
