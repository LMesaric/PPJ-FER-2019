import java.util.Scanner;

public class LexerGenerator {

    public static void main(String[] args) {
        StringBuilder input = new StringBuilder();
        try (Scanner sc = new Scanner(System.in)) {
            while (sc.hasNext()) {
                input.append(sc.nextLine()).append("\n");
            }
        }

    }

}
