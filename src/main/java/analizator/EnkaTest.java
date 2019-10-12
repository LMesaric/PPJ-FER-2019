package analizator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EnkaTest {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("./src/main/java/analizator/enkaTable.txt");
        Enka enka = buildEnka(path);
        String simulate = "1abc";
        System.out.println(simulateEnka(enka, simulate));
    }

    private static Enka buildEnka(Path path) throws IOException {
        String table = new String(Files.readAllBytes(path));
        Enka enka = new Enka();
        enka.buildFromTable(table);
        return enka;
    }

    private static EnkaStatus simulateEnka(Enka enka, String expression) {
        EnkaStatus status = null;
        enka.reset();
        for (char c : expression.toCharArray()) {
            status = enka.performTransition(c);
        }
        return status;
    }

}
