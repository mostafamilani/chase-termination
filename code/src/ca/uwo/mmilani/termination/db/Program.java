package ca.uwo.mmilani.termination.db;

import ca.uwo.mmilani.termination.primitives.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Program {
    public String name;
    public Set<TGD> tgds = new HashSet<>();
    public Set<EGD> egds = new HashSet<>();
    public Set<NC> ncs = new HashSet<>();
    public Database edb;
    public int nExistential = 0;
    public int nComponents = 0;
    public int nSpecialComponents = 0;
    public Schema schema;
    public Set<FCComponent> sfccs;
    public int dpEdges = 0;
    public int dpEsEdges = 0;
    public Map<Position, Node> depGraph;
    public Map<String, Object> stats = new HashMap<>();
    public static final int LINEAR = 1;
    public static final int NON_LINEAR = 0;
    public static final int SIMPLE_LINEAR = 2;
    public int eclass = SIMPLE_LINEAR;

    public Map<String,String> externalParams = new HashMap<>();

    public int size = 0;

    public Program() {
        schema = new Schema();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (TGD tgd : tgds) {
            s.append(tgd).append("\n");
        }
        return s.toString();
    }
}
