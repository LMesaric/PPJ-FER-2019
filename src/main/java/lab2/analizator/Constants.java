package lab2.analizator;

// Two exact copies of Constants class exist purely because of the way online evaluator is set up.
class Constants {
    static final String ACTION_TABLE_PATH = "./src/main/java/lab2/analizator/action.ser";

    static final String NEW_STATE_TABLE_PATH = "./src/main/java/lab2/analizator/newState.ser";

    static final String SYNCHRONIZATIONAL_SYMBOLS_PATH = "./src/main/java/lab2/analizator/synchronizational.ser";

    static final String EPSILON = "$";

    static final String INITIAL_STATE = "<%>";

    static final String MARK = "*";

    static final String END = "#";

    static final String STACK_END = "£";
}
