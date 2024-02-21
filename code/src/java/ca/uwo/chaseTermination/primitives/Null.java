package ca.uwo.chaseTermination.primitives;

import java.util.HashMap;
import java.util.Map;

public class Null extends Term {
    public static int INDEX = 1;
    private static Map<String, Null> nulls = new HashMap<>();
    public boolean frozen = false;
    public boolean confirmed = false;
    public int index = 0;

    private Null(String label, int index) {
        this.label = label;
        this.index = index;
    }

    static Null fetch(String s) {
        if (!nulls.containsKey(s))
            throw new RuntimeException("Invalid null label (" + s + ")");
        return nulls.get(s);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Null && toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
