package lab4;

import java.util.*;
import java.util.stream.Collectors;

public class FunctionContext {

    String functionName;
    String functionLabel;
    Deque<Map<String, Variable>> localsScopeStack = new ArrayDeque<>();
    Deque<Integer> lastOffsets = new ArrayDeque<>();
    int currentOffset = -4;
    Deque<LoopGenerationContext> loops = new ArrayDeque<>();
    boolean returnWritten = false;

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
        var.addressingOffset = currentOffset;
        currentOffset -= var.memSize;
        localsScopeStack.peek().put(var.name, var);
    }

    public void putNewScope() {
        lastOffsets.push(currentOffset);
        localsScopeStack.push(new LinkedHashMap<>());
    }

//    public void putNewScope(Collection<Variable> variables) {
//        lastOffsets.push(currentOffset);
//        for (Variable var : variables) {
//            currentOffset -= 4;
//            var.addressingOffset = currentOffset;
//        }
//        localsScopeStack.push(variables.stream().collect(Collectors.toMap(var -> var.name, var -> var)));
//    }

    public Map<String, Variable> getLastScope() {
        return localsScopeStack.peekLast();
    }

    public int getLastScopeMemSize() {
        if (localsScopeStack.peekFirst() == null) return 0;
        return localsScopeStack.peekFirst().values().stream().mapToInt(v -> v.memSize).sum();
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

    public void createLoop() {
        loops.push(new LoopGenerationContext());
    }

    public void exitLoop() {
        loops.pop();
    }

    public LoopGenerationContext lastLoop() {
        return loops.peekLast();
    }

    public void addCommand(String command) {
        addCommand(command, null);
    }

    public void addCommand(String command, String label) {
        commands.add(BuilderUtil.buildLine(command, label));
    }

    public List<String> getCommands() {
        return commands;
    }

}
