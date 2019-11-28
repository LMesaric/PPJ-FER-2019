package lab2;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static lab2.TestUtil.TESTCASES_DIR_DFA;

class DFABuildTest {

    @Test
    void test() throws IOException {
        //assertTrue(test("test1"));
        //assertTrue(test("test2"));
    }

    private boolean test(String fileName) throws IOException {
        Path pathOut = Paths.get(TESTCASES_DIR_DFA + fileName + ".out");
        DFA dfa = new DFA(TestUtil.createENFA(fileName));
        String s = dfa.print();
        String[] arr1 = s.split("\n");
        String[] arr2 = new String(Files.readAllBytes(pathOut)).split("\n");
        for (int i = 0; i < arr2.length; i++) {
            arr2[i] = arr2[i].replace("\r", "");
        }
        for (int i = 0; i < arr1.length && i < arr2.length; i++) {
            if (!arr1[i].equals(arr2[i])) {
                System.out.println("Arr1 (" + i + ") :" + arr1[i]);
                //System.out.println("Arr2 (" + i + ") :" + arr2[i]);
            }
        }
        return Arrays.equals(arr1, arr2);
    }

}
