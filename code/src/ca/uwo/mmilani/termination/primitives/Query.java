package ca.uwo.mmilani.termination.primitives;

import ca.uwo.mmilani.termination.db.Program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Query<B> extends Rule<B, Atom> {
    public List<Variable> headVariables = new ArrayList<>();
    @Override
    public String toString() {
        return head.toString() + "?-" + body.toString();
    }

    public void addProgram(Program program) {

    }
}
