package lab2.analizator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;

public class ObjectReaderUtil {

    @SuppressWarnings("unchecked")
    public static Set<String> readSetFromFile(String filename) throws IOException {
        return (Set<String>) readObjectFromFile(filename);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<Integer, Map<String, T>> readMapFromFile(String filename) throws IOException {
        return (Map<Integer, Map<String, T>>) readObjectFromFile(filename);
    }

    public static Object readObjectFromFile(String filename) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

}
