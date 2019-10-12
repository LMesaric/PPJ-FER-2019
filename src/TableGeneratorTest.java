import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TableGeneratorTest {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("./src/analizator/enkaTable.txt");
        String expression = "((1|2|3)ab|c)c";
        generateTable(path, expression);
    }

    private static void generateTable(Path path, String expression) throws IOException {
        TableGenerator generator = new TableGenerator();
        String table = generator.buildTable(expression);
        Files.write(path, table.getBytes());
    }

}
