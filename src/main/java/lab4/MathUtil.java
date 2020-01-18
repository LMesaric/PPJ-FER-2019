package lab4;

class MathUtil {

    static FunctionContext generateMultiplicationImplementation() {
        FunctionContext impl = new FunctionContext();
        impl.functionName = "O_MUL";

        // R0 * R1 -> R6
        impl.addCommand("LOAD R1, (SP+4)", impl.functionName);
        impl.addCommand("LOAD R0, (SP+8)");

        impl.addCommand("MOVE 0, R6  ; result");
        impl.addCommand("MOVE 0, R2  ; sign flag");

        impl.addCommand("CMP R1, 0");
        impl.addCommand("JP_EQ O_MUL_RET");
        impl.addCommand("JP_SGT O_MUL_LOOP");

        impl.addCommand("MOVE 1, R2");
        impl.addCommand("XOR R1, -1, R1");
        impl.addCommand("ADD R1, 1, R1");

        impl.addCommand("CMP R1, 0", "O_MUL_LOOP");
        impl.addCommand("JP_EQ O_MUL_END");
        impl.addCommand("ADD R0, R6, R6");
        impl.addCommand("SUB R1, 1, R1");
        impl.addCommand("JP O_MUL_LOOP");

        impl.addCommand("CMP R2, 0", "O_MUL_END");
        impl.addCommand("JP_EQ O_MUL_RET");
        impl.addCommand("XOR R6, -1, R6");
        impl.addCommand("ADD R6, 1, R6");

        impl.addCommand("RET", "O_MUL_RET");
        return impl;
    }

    static FunctionContext generateDivisionImplementation() {
        FunctionContext impl = new FunctionContext();
        impl.functionName = "O_DIV";

        // R0 / R1 -> R6
        impl.addCommand("LOAD R1, (SP+4)", impl.functionName);
        impl.addCommand("LOAD R0, (SP+8)");

        impl.addCommand("MOVE 0, R6  ; result");
        impl.addCommand("MOVE 0, R2  ; sign flag");

        impl.addCommand("CMP R1, 0");
        impl.addCommand("JP_EQ O_DIV_RET");
        impl.addCommand("JP_SGT O_DIV_POS");

        impl.addCommand("XOR R2, 1, R2");
        impl.addCommand("XOR R1, -1, R1");
        impl.addCommand("ADD R1, 1, R1");

        impl.addCommand("CMP R0, 0", "O_DIV_POS");
        impl.addCommand("JP_EQ O_DIV_RET");
        impl.addCommand("JP_SGT O_DIV_LOOP");

        impl.addCommand("XOR R2, 1, R2");
        impl.addCommand("XOR R0, -1, R0");
        impl.addCommand("ADD R0, 1, R0");

        impl.addCommand("CMP R0, R1", "O_DIV_LOOP");
        impl.addCommand("JP_SLT O_DIV_END");
        impl.addCommand("ADD R6, 1, R6");
        impl.addCommand("SUB R0, R1, R0");
        impl.addCommand("JP O_DIV_LOOP");

        impl.addCommand("CMP R2, 0", "O_DIV_END");
        impl.addCommand("JP_EQ O_DIV_RET");
        impl.addCommand("XOR R6, -1, R6");
        impl.addCommand("ADD R6, 1, R6");

        impl.addCommand("RET", "O_DIV_RET");
        return impl;
    }

    static FunctionContext generateModuloImplementation() {
        FunctionContext impl = new FunctionContext();
        impl.functionName = "O_MOD";

        // R6 % R1 -> R6 (R6 >= 0, R1 > 0)
        impl.addCommand("LOAD R1, (SP+4)", impl.functionName);
        impl.addCommand("LOAD R6, (SP+8)");

        impl.addCommand("CMP R6, 0");
        impl.addCommand("JP_EQ O_MOD_RET");

        impl.addCommand("CMP R6, R1", "O_MOD_LOOP");
        impl.addCommand("JP_SLT O_MOD_RET");
        impl.addCommand("SUB R6, R1, R6");
        impl.addCommand("JP O_MOD_LOOP");

        impl.addCommand("RET", "O_MOD_RET");
        return impl;
    }

}
