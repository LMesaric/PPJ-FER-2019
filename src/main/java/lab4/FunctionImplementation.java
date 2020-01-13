package lab4;

import java.util.*;
import java.util.stream.Collectors;

public class FunctionImplementation {

    String functionName;
    String functionLabel;
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

    public void putNewVariable(Variable var) {
        currentOffset -= 4;
        var.addressingOffset = currentOffset;
        localsScopeStack.peek().put(var.name, var);
    }

    public void putNewScope() {
        lastOffsets.push(currentOffset);
        localsScopeStack.push(new LinkedHashMap<>());
    }

    public void putNewScope(Collection<Variable> variables) {
        lastOffsets.push(currentOffset);
        for (Variable var : variables) {
            currentOffset -= 4;
            var.addressingOffset = currentOffset;
        }
        localsScopeStack.push(variables.stream().collect(Collectors.toMap(var -> var.name, var -> var)));
    }

    public void removeLastScope() {
        localsScopeStack.pop();
        currentOffset = lastOffsets.pop();
    }

    public Variable findVariable(String variableName) {
        Iterator<Map<String, Variable>> it = localsScopeStack.descendingIterator();
        while (it.hasNext()) {
            Map<String, Variable> scope = it.next();
            if (scope.containsKey(variableName)) return scope.get(variableName);
        }
        return null;
    }

    public void addCommand(String command) {
        commands.add(command);
    }

    public List<String> getCommands() {
        return commands;
    }


}
