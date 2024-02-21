package ca.uwo.chaseTermination.primitives;

public class Constant extends Term {

    public Constant(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
