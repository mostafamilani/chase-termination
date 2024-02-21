package ca.uwo.chaseTermination.graph;

import java.util.Objects;

public class Edge {
    public Node destination;
    public boolean special;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return special == edge.special && Objects.equals(destination, edge.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, special);
    }

    @Override
    public String toString() {
        return "[" + destination.p +
                "] <-- [" +
                "], special=" + special;
    }
}
