package ca.uwo.chaseTermination.graph;

public class FCCNode extends Node {

    public FCCNode(){
        super();
    }
    public boolean special = false;
    public boolean visited = false;
    public FCComponent component;

    public boolean marked = false;

    @Override
    public String toString() {
        return component.toString()  + " + " + nextEdges;
    }

}
