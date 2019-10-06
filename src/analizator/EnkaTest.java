package analizator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class EnkaTest {

    public static void main(String[] args) throws IOException {
        Enka enka = new Enka();
        Path path = Paths.get("./src/analizator/enkaTable.txt");
        String table = enka.buildTable("((1|2|3)ab|c)c");
        Files.write(path, table.getBytes(), StandardOpenOption.CREATE);
    }

}
