package ca.uwo.chaseTermination.primitives;

import java.util.Objects;

public class Variable extends Term {
    final static String DONT_CARE = "*";
    boolean body = false;
    boolean head = false;
    boolean existential;

    public Variable(String name) {
        this.label = name;
    }

    public boolean isBody() {
        return body;
    }

    public boolean isExistential() {
        return existential;
    }

    public void setExistential(boolean existential) {
        this.existential = existential;
    }

    public void setBody() {
        this.body = true;
    }

    public boolean dontCare() {
        return label.equals(DONT_CARE);
    }

    public boolean isHead() {
        return head;
    }

    public void setHead() {
        this.head = true;
    }

    @Override
    public boolean equals(Object o) {
        return Objects.equals(o.toString(), this.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return label;
    }
}
