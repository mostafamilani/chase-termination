package ca.uwo.chaseTermination.tools.syntax;

import ca.uwo.chaseTermination.primitives.Assignment;
import ca.uwo.chaseTermination.primitives.Atom;
import ca.uwo.chaseTermination.primitives.Term;

public class AtomGadget {
    public static Assignment mapTo(Atom a, Atom b) {
        if (b.predicate != a.predicate) return null;
        Assignment answer = new Assignment();
        for (int i = 0; i < b.terms.toArray().length; i++) {
            Term tf = (Term) b.terms.toArray()[i];
            Term tq = (Term) a.terms.toArray()[i];
            if (!answer.tryToMap(tq, tf))
                return null;
        }
        return answer;
    }
}
