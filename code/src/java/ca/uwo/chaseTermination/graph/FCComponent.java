package ca.uwo.chaseTermination.graph;

import java.util.HashSet;
import java.util.Set;

public class FCComponent {
    public boolean special = false;
    public Set<Node> members = new HashSet<>();

    @Override
    public String toString() {
        return members.toString();
    }
}
