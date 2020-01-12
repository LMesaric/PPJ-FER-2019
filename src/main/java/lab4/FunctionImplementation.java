package lab4;

import java.util.*;
import java.util.stream.Collectors;

public class FunctionImplementation {

    String functionName;
    Deque<Map<String, Variable>> localsScopeStack = new ArrayDeque<>();
    Deque<Integer> lastOffsets = new ArrayDeque<>();
    int currentOffset = 0;

    List<String> commands = new ArrayList<>();

    public void setParameters(Collection<Variable> params) {
        Map<String, Variable> parameters = new LinkedHashMap<>();
        int offset = 4 * params.size();                 // Last parameter should be on +4 offset
        for (Variable var : params) {
            var.addressingOffset = offset;
            offset -= 4;
            parameters.put(var.name, var);
        }
        localsScopeStack.push(parameters);
    }

    public void putNewScope(Collection<Variable> variables) {
        lastOffsets.push(currentOffset);
        for (Variable var : variables) {
            currentOffset -= 4;
            var.addressingOffset = currentOffset;
        }
        localsScopeStack.push(variables.stream().collect(Collectors.toMap(var -> var.name, var -> var)));
    }

    public void removeLastScope(Collection<Variable> variables) {
        localsScopeStack.pop();
        currentOffset = lastOffsets.pop();
    }

    public void addCommand(String command) {
        commands.add(command);
    }

    public List<String> getCommands() {
        return commands;
    }


}
