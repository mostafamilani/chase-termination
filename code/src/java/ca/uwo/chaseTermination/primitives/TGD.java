package ca.uwo.chaseTermination.primitives;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TGD extends Rule<Conjunct, Conjunct> {
    public Set<Variable> existentialVars = new HashSet<>();

    public Variable fetchVariable(String s, boolean body) {
        Variable variable;
        if (!variables.containsKey(s)) {
            variable = this.fetchVariable(s);
            variables.put(s, variable);
        } else
            variable = variables.get(s);
        if (body)
            variable.setBody();
        else
            variable.setHead();
        if (!variable.isBody() && variable.isHead())
            variable.setExistential(true);
        return variable;
    }

    public void initVars() {
        List<PositiveAtom> bodyAtoms = body.getAtoms();
        for (PositiveAtom atom : bodyAtoms) {
            List<Term> terms = atom.terms;
            for (Term term : terms) {
                Variable v = fetchVariable(term.label);
                v.body = true;
                variables.put(term.label, v);
            }
        }
        List<PositiveAtom> headAtoms = head.getAtoms();
        for (PositiveAtom atom : headAtoms) {
            List<Term> terms = atom.terms;
            for (Term term : terms) {
                Variable v = fetchVariable(term.label);
                v.head = true;
                variables.put(term.label, v);
            }
        }
    }

    public boolean isFrontier(Variable variable) {
        return head.getVariables().contains(variable);
    }

    @Override
    public String toString() {
        return head + ":-" + body;
    }

    public String predicateString() {
        return body.getAtoms().get(0).predicate.toString() + head.getAtoms().get(0).predicate.toString();
    }
}
