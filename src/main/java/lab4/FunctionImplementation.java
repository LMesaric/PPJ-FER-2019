package lab4;

import java.util.*;

public class FunctionImplementation {

    String functionName;
    Map<String, Variable> locals = new LinkedHashMap<>();
    Map<String, Variable> parameters = new LinkedHashMap<>();

    List<String> commands = new ArrayList<>();

    public void addCommand(String command) {
        commands.add(command);
    }

    public List<String> getCommands() {
        return commands;
    }



}
