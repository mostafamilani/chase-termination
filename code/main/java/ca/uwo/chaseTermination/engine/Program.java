package ca.uwo.chaseTermination.engine;

import ca.uwo.chaseTermination.executer.SyntacticAnalyzer;
import ca.uwo.chaseTermination.primitives.Rule;
import ca.uwo.chaseTermination.primitives.TGD;
import ca.uwo.chaseTermination.primitives.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Program {
    public Map<String, Object> stats = new HashMap<>();

    public String name;
    public Set<TGD> tgds = new HashSet<>();

    public Database database;

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (TGD tgd : tgds) {
            s.append(tgd).append("\n");
        }
        return s.toString();
    }

    public void addRule(Rule rule) {
        if (rule instanceof TGD tgd) {
            this.tgds.add(tgd);
        } else throw new RuntimeException("Rule type is not supported! " + rule);
    }

    public boolean isLinear() {
        for (TGD tgd : tgds) {
            if (tgd.body.getAtoms().size() != 1)
                return false;
        }
        return true;
    }

    public boolean isSimpleLinear() {
        for (TGD tgd : tgds) {
            if (tgd.body.getAtoms().size() != 1)
                return false;
            for (Variable variable : tgd.variables.values()) {
                if (SyntacticAnalyzer.isRepeated(variable, tgd))
                    return false;
            }
        }
        return true;
    }

    public void removeNonLinearRules() {
        Set<TGD> linearTGDs = new HashSet<>();
        for (TGD tgd : tgds) {
            if (tgd.body.getAtoms().size() == 1)
                linearTGDs.add(tgd);
        }
        tgds = linearTGDs;
    }

    public void increamentStat(String key) {
        if (!stats.containsKey(key)) stats.put(key, 0);
        stats.put(key, ((Integer) stats.get(key)) + 1);
    }
}
