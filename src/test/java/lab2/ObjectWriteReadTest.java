package lab2;

import lab2.analizator.ObjectReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class ObjectWriteReadTest {

    private static String path = "./src/test/java/lab2/serialized.ser";

    @AfterAll
    static void deleteGeneratedFile() throws IOException {
        Files.deleteIfExists(Paths.get(path));
    }

    @Test
    void test() throws IOException {
        Map<String, Set<String>> mapOne = createExampleMap();
        ObjectWriterUtil.writeObjectToFile(mapOne, path);
        Map<String, Set<String>> mapRead = ObjectReaderUtil.readMapFromFile(path);
        Assertions.assertEquals(mapRead, createExampleMap());
    }

    private Map<String, Set<String>> createExampleMap() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("<A>", new HashSet<>(Arrays.asList("a", "c", "<D>")));
        map.put("<C>", new HashSet<>(Arrays.asList("b", "i")));
        map.put("<E>", new HashSet<>(Arrays.asList("<V>", "u", "l", "<P>")));
        map.put("x", new HashSet<>());
        return map;
    }

}
