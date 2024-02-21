package ca.uwo.chaseTermination.primitives;

//import ca.uwo.chaseTermination.engine.Program;

import ca.uwo.chaseTermination.engine.Program;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Rule<B, H> {
    public B body;
    public H head;
    public Program program;
    public Set<Variable> headVariables = new HashSet<>();

    public Map<String, Variable> variables = new HashMap<>();
    int index = 0;

    public Variable fetchVariable(String name) {
        if (!variables.containsKey(name)) {
            Variable variable = new Variable(name);
            variables.put(name, variable);
            return variable;
        }
        return variables.get(name);
    }

    public Variable fetchNewVariable() {
        return fetchVariable("x" + ++index/*Integer.toHexString(index)*/);
    }

    public Variable fetchVariable(String s, boolean body) {
        boolean existential = false;
        if (!variables.containsKey(s)) {
            variables.put(s, fetchNewVariable());
            existential = !body;
        }
        Variable variable = variables.get(s);
        if (body)
            variable.setBody();
        else {
            headVariables.add(variable);
            variable.setHead();
        }
        variable.setExistential(existential);
        return variable;
    }

    public Variable getDontCare() {
        return fetchVariable(Variable.DONT_CARE);
    }
}
