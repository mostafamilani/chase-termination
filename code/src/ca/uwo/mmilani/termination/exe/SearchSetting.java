package ca.uwo.mmilani.termination.exe;

import ca.uwo.mmilani.termination.primitives.FCComponent;
import ca.uwo.mmilani.termination.primitives.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class SearchSetting {
    public Stack<Node> stack = new Stack<>();
    public int globalIndex = 0;
    public Set<FCComponent> components = new HashSet<>();
}
