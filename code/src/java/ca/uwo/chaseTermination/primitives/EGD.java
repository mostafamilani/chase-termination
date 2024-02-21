package ca.uwo.chaseTermination.primitives;

public class EGD extends Rule<Conjunct, EqualityAtom> {

    @Override
    public String toString() {
        return head + ":-" + body;
    }
}
