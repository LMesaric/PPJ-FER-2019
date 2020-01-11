package lab4;

import java.util.ArrayList;
import java.util.List;

public class ImplementationContext {

    List<String> commands = new ArrayList<>();

    public void addCommand(String command) {
        commands.add(command);
    }

    public List<String> getCommands() {
        return commands;
    }

}
