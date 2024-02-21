package ca.uwo.chaseTermination.primitives;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Assignment {
    public Map<Term, Term> mappings = new HashMap<>();
    @Getter
    @Setter
    public int level = 0;

    public boolean tryToMap(Term t1, Term t2) {
        if (t1 instanceof Variable && ((Variable) t1).dontCare())
            return true;
        if (t1 instanceof Constant)
            return t1 == t2;
        if (mappings.containsKey(t1) && mappings.get(t1) != t2)
            return false;
        else {
            mappings.put(t1, t2);
            return true;
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Term term : mappings.keySet()) {
            s.append(term).append(" -> ").append(mappings.get(term)).append(", ");
        }
        return s + "\n";
    }

    @Override
    public Object clone() {
        Assignment assignment = new Assignment();
        assignment.level = this.level;
        assignment.mappings = new HashMap<>();
        Set<Term> keySet = mappings.keySet();
        for (Term key : keySet)
            assignment.mappings.put(key, this.mappings.get(key));
        return assignment;
    }

    @Override
    public boolean equals(Object o) {
        Assignment v = (Assignment) o;
        if (v == null || !v.mappings.keySet().equals(this.mappings.keySet()))
            return false;
        for (Term key : v.mappings.keySet()) {
            if (!v.mappings.get(key).equals(mappings.get(key)))
                return false;
        }
        return true;

    }

    @Override
    public int hashCode() {
        return (this + "").hashCode();
    }

    public Map<Term, Term> getMappings() {
        return Collections.unmodifiableMap(mappings);
    }

    public void put(Term v1, Term v2) {
        mappings.put(v1, v2);
    }
}
