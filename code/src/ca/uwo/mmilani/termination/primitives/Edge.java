package ca.uwo.mmilani.termination.primitives;

import java.util.*;

public class Edge {
    public Node destination;
    public boolean special;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return special == edge.special && /*Objects.equals(source, edge.source) && */Objects.equals(destination, edge.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, special);
    }
}
