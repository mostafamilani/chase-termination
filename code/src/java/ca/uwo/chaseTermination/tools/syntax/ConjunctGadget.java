package ca.uwo.chaseTermination.tools.syntax;

import ca.uwo.chaseTermination.parser.Parser;
import ca.uwo.chaseTermination.primitives.*;

import java.util.HashSet;
import java.util.Set;

public class ConjunctGadget {
    public static boolean mapTo(Conjunct c1, Conjunct c2) {
        Set<Assignment> assignments = generateAllAssignments(new HashSet<>(c1.getVariables()), new HashSet<>(c2.getVariables()));
        for (Assignment assignment : assignments) {
            if (equates(assignment, c1, c2)) return true;
        }
        return false;
    }

    private static boolean equates(Assignment assignment, Conjunct c1, Conjunct c2) {
        for (PositiveAtom atom : c1.getAtoms()) {
            PositiveAtom apply = atom.apply(assignment);
            boolean covered = false;
            for (PositiveAtom next : c2.getAtoms()) {
                if (Atom.equalsMasked(apply, next)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) return false;
        }
        return true;
    }

    private static Set<Assignment> generateAllAssignments(Set<Variable> vs1, Set<Variable> vs2) {
        HashSet<Assignment> assignments = new HashSet<>();
        if (vs1.size() == 1 || vs2.size() == 1) {
            for (Variable v1 : vs1) {
                for (Variable v2 : vs2) {
                    Assignment e = new Assignment();
                    e.put(v1, v2);
                    assignments.add(e);
                }
            }
        } else {
            for (Variable v1 : vs1) {
                for (Variable v2 : vs2) {
                    HashSet<Variable> nvs1 = new HashSet<>(vs1);
                    nvs1.remove(v1);
                    HashSet<Variable> nvs2 = new HashSet<>(vs2);
                    nvs2.remove(v2);
                    Set<Assignment> aset = generateAllAssignments(nvs1, nvs2);
                    for (Assignment assignment : aset) {
                        assignment.put(v1, v2);
                    }
                    assignments.addAll(aset);
                }
            }
        }
        return assignments;
    }

    public static void main(String[] args) {
        String s1 = "p(x,y)";
        String s2 = "p(*,b),p(a,*)";
        CQ q1 = new CQ();
        CQ q2 = new CQ();
        Conjunct c1 = Parser.parseConjunct(s1, true, null, q1);
        Conjunct c2 = Parser.parseConjunct(s2, true, null, q2);
        boolean b1 = mapTo(c1, c2);
        boolean b2 = mapTo(c2, c1);
        System.out.println(c1 + " -> " + c2 + " : " + b1);
        System.out.println(c2 + " -> " + c1 + " : " + b2);
    }
}
