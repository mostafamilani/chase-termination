package ca.uwo.chaseTermination.primitives;

import lombok.NoArgsConstructor;

import java.util.*;

public class Conjunct {
    private List<PositiveAtom> atoms = new ArrayList<>();
    private List<Variable> variables;


    public Conjunct() {
        atoms = new ArrayList<>();
    }

    public Conjunct(Conjunct conjunct, PositiveAtom toRemove) {
        atoms = new ArrayList<>(conjunct.getAtoms());
        atoms.remove(toRemove);
    }

    @Override
    public String toString() {
        if (atoms.isEmpty()) return "";
        StringBuilder s = new StringBuilder();
        for (Atom atom : atoms) {
            s.append(atom).append(",");
        }
        return s.substring(0, s.length() - 1);
    }

    public Map<Predicate, Set<PositiveAtom>> getPredicates() {
        HashMap<Predicate, Set<PositiveAtom>> atoms = new HashMap<>();
        for (PositiveAtom atom : this.atoms) {
            if (!atoms.containsKey(atom.predicate))
                atoms.put(atom.predicate, new HashSet<>());
            atoms.get(atom.predicate).add(atom);
        }
        return atoms;
    }

    public List<PositiveAtom> getAtoms() {
        return Collections.unmodifiableList(atoms);
    }

    public void add(PositiveAtom atom) {
        if (atoms.stream().anyMatch(a -> Atom.equalsMasked(atom, a)))
            return;
        atoms.add(atom);
        sort();
    }

    public void addAll(List<PositiveAtom> atomsToAdd) {
        for (PositiveAtom atom : atomsToAdd)
            add(atom);
        sort();
    }

    void sort() {
        atoms.sort(Comparator.comparing(Atom::toString));
    }

    public List<Variable> getVariables() {
        if (variables == null)
            setVariables();
        return variables;
    }

    private void setVariables() {
        variables = new ArrayList<>();
        for (PositiveAtom atom : atoms)
            variables.addAll(atom.getVariables());
    }

    public void addFirst(PositiveAtom atom) {
        atoms.add(0, atom);
    }
}
