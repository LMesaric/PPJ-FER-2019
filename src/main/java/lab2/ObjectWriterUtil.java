package lab2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

class ObjectWriterUtil {

    static void writeObjectToFile(Object object, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(object);
        }
    }

}
