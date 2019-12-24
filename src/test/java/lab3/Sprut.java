package lab3;

import common.SprutEvaluator;

import java.io.IOException;

class Sprut {

    static final String ANALYZER_CLASS = "lab3.SemantickiAnalizator";
    static final String TESTCASES_DIR = "src/test/resources/testcases_Semantics";

    public static void main(String[] args) throws IOException, InterruptedException {
        SprutEvaluator sprut = new SprutEvaluator(System.out::println, TESTCASES_DIR, ANALYZER_CLASS,
                ".*\\.in", ".*\\.out");
        sprut.evaluateAll();
    }

}
