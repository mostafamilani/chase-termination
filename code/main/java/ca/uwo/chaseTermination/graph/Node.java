package ca.uwo.chaseTermination.graph;

import ca.uwo.chaseTermination.primitives.Position;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Node {
    public Position p;
    public Set<Edge> nextEdges = new HashSet<>();
    public int index = -1;
    public int lowLink;

    public Node() {
    }

    public Node(Position position) {
        this.p = position;
    }

    public Edge nextEdge(Node node) {
        return nextEdges.stream()
                .filter(edge -> edge.destination.equals(node))
                .findFirst()
                .orElse(null);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(p, node.p);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p);
    }

    public void addNext(Node next, boolean special) {
        Edge edge = nextEdge(next);
        if (edge == null) {
            edge = new Edge();
            edge.destination = next;
        }
        edge.special = edge.special || special;
        nextEdges.add(edge);
    }

    public Set<Node> nextNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nextEdges.forEach(edge -> nodes.add(edge.destination));
        return nodes;
    }

    public Set<Node> nextSpecialNodes() {
        HashSet<Node> nodes = new HashSet<>();
        for (Edge next : nextEdges)
            if (next.special)
                nodes.add(next.destination);
        return nodes;
    }

    @Override
    public String toString() {
        return this.p.toString();
    }

    public String getName() {
        return this.p.predicate.name;
    }
}
