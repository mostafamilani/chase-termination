package primitives;

import java.util.*;

public class Node {
    public Position p;
    public Set<Edge> nexts = new HashSet<>();
    public Set<Edge> prevs = new HashSet<>();
    public int index = -1;
    public boolean onStack;
    public int lowLink;

    public Node(Position position) {
        this.p = position;
    }

    public Edge nextEdge(Node node) {
        for (Edge edge : nexts) {
            if (edge.destination.equals(node))
                return edge;
        }
        return null;
    }

    public Edge prevEdge(Node node) {
        for (Edge edge : prevs) {
            if (edge.destination.equals(node))
                return edge;
        }
        return null;
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

    public boolean addNext(Node next, boolean special) {
        Edge edge = nextEdge(next);
        boolean added = false;
        if (edge == null) {
            edge = new Edge();
            edge.destination = next;
            edge.special = special;
            added = true;
            nexts.add(edge);
        }
        Edge pEdge = next.prevEdge(this);
        if (pEdge == null) {
            pEdge = new Edge();
            pEdge.destination = this;
            next.prevs.add(pEdge);
            added = true;
        }
        return added;
    }

    public Set<Node> nextNodes(boolean reverse) {
        HashSet<Node> nodes = new HashSet<>();
        Set<Edge> edges = reverse? prevs : nexts;
        for (Edge next : edges) {
            nodes.add(next.destination);
        }
        return nodes;
    }
}
