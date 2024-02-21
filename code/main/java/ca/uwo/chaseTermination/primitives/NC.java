package ca.uwo.chaseTermination.primitives;

public class NC extends Rule<Conjunct, FalseAtom> {

    public Variable fetchVariable(String s, boolean body) {
        boolean existential = false;
        if (!variables.containsKey(s)) {
            variables.put(s, fetchNewVariable());
            existential = !body;
        }
        Variable variable = variables.get(s);
        if (body)
            variable.setBody();
        else
            variable.setHead();
        variable.setExistential(existential);
        return variable;
    }

    @Override
    public String toString() {
        return ":-" + body;
    }
}
