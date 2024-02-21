package ca.uwo.chaseTermination.primitives;

import java.util.ArrayList;
import java.util.List;

public abstract class Query<B> extends Rule<B, Atom> {
    public List<Variable> headVariables = new ArrayList<>();

    @Override
    public String toString() {
        return head.toString() + "?-" + body.toString();
    }

}
