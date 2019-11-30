package lab2;

import lab2.analizator.ObjectReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ObjectWriteReadTest {

    private static final String path = "./src/test/java/lab2/serialized.ser";

    @AfterAll
    static void deleteGeneratedFile() throws IOException {
        Files.deleteIfExists(Paths.get(path));
    }

    @Test
    void test() throws IOException {
        Map<Integer, Map<String, Object>> mapOne = createExampleMap();
        ObjectWriterUtil.writeObjectToFile(mapOne, path);
        Map<Integer, Map<String, Object>> mapRead = ObjectReaderUtil.readMapFromFile(path);
        Assertions.assertEquals(mapRead, createExampleMap());
    }

    private Map<Integer, Map<String, Object>> createExampleMap() {
        Map<Integer, Map<String, Object>> map = new HashMap<>();

        map.put(7, new HashMap<>());
        map.get(7).put("a", new Move(3));
        map.get(7).put("cx", new Put(1));

        map.put(1, new HashMap<>());
        map.get(1).put("b", new Accept());
        map.get(1).put("Du", new Move(17));
        map.get(1).put("tkv", new Reduce(new Production("<X>", Arrays.asList("a", "<U>", "oub"))));

        map.put(3, new HashMap<>());
        map.get(3).put("oof", new Put(6));
        map.get(3).put("oeuf", new Reduce(new Production("<Z>", Collections.emptyList())));

        map.put(6, new HashMap<>());    // empty

        return map;
    }

}
