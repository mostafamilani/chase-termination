package ca.uwo.chaseTermination.primitives;

import ca.uwo.chaseTermination.engine.Program;

import java.util.Objects;

public class Predicate {
    public String name;
    public int arity;
    public Program program;
    public boolean extensional = false;

    public Predicate(String name, int arity, Program program) {
        this.name = name;
        this.arity = arity;
        this.program = program;
    }

    public Predicate(String name, int arity) {
        this.name = name;
        this.arity = arity;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isAdorned() {
        return name.contains("^");
    }

    public String getAdornment() {
        if (!isAdorned())
            return null;
        return name.substring(name.indexOf("^") + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Predicate predicate = (Predicate) o;
        return arity == predicate.arity && Objects.equals(name, predicate.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arity);
    }
}
