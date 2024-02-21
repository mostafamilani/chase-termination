package ca.uwo.chaseTermination.primitives;

import java.util.List;

public class Fact extends PositiveAtom {
    public Fact(Predicate p, List<Term> ts) {
        super(p, ts);
    }


    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }
}
