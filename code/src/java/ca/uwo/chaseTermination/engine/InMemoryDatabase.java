package ca.uwo.chaseTermination.engine;

import ca.uwo.chaseTermination.parser.Parser;
import ca.uwo.chaseTermination.primitives.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.UnaryOperator;

public class InMemoryDatabase extends Database {
    Map<String, Fact> facts = new HashMap<>();

    public InMemoryDatabase() {

    }

    public Fact[] getFacts() {
        Object[] array = facts.values().toArray();
        return Arrays.copyOf(array, array.length, Fact[].class);
    }


    public Fact addFact(Predicate predicate, List<Term> terms) {
        Fact f = new Fact(predicate, terms);
        String s = f.toString();
        if (!facts.containsKey(s))
            facts.put(s, f);
        Fact fact = facts.get(s);
        incrementRecordCount(predicate);
        return fact;
    }

    private void incrementRecordCount(Predicate predicate) {
        if (!recordCount.containsKey(predicate)) recordCount.put(predicate, 0);
        int count = recordCount.get(predicate);
        recordCount.put(predicate, count+1);
    }

    public void addFact(Fact fact) {
        addFact(fact.predicate, fact.terms);
    }

    @Deprecated
    public void checkNullChange(Null n, Term t) {
        Map<String, Fact> updatedFacts = new HashMap<String, Fact>();
        for (Fact fact : facts.values()) {
            final boolean[] changed = {false};
            String key = fact.toString();
            fact.terms.replaceAll(new UnaryOperator<Term>() {
                @Override
                public Term apply(Term term) {
                    if (term == n) {
                        changed[0] = true;
                        return t;
                    } else
                        return term;
                }
            });
            if (changed[0]) {
                updatedFacts.put(key, fact);
            }
        }
        for (String key : updatedFacts.keySet()) {
            facts.remove(key);
            Fact newFact = updatedFacts.get(key);
            facts.put(newFact.toString(), newFact);
        }
    }

    @Deprecated
    public Fact fetch(String s) {
        Fact fact;
        if (!facts.containsKey(s)) {
            fact = (Fact) Parser.parse(s, false, program);
            facts.put(s, fact);
        } else
            fact = facts.get(s);
        return fact;
    }

    @Override
    public Map<Predicate, Set<String>> findShapes() {
        Map<Predicate, Set<String>> shapes = new HashMap<>();
        for (Fact f : facts.values()) {
            Predicate predicate = f.predicate;
            List<Term> terms = f.terms;
            shapes.computeIfAbsent(predicate, k -> new HashSet<>());
            StringBuilder ann = getShapes(terms);
            Set<String> predicateShapes = shapes.get(predicate);
            predicateShapes.add(ann.toString());
            shapes.put(predicate, predicateShapes);
        }
        return shapes;
    }

    private StringBuilder getShapes(List<Term> terms) {
        ArrayList<String> values = new ArrayList<>();
        StringBuilder ann = new StringBuilder();
        int max = 1;
        for (Term term : terms) {
            String newString = term.label;
            if (values.contains(newString))
                ann.append(Integer.toHexString((values.indexOf(newString) + 1)));
            else {
                ann.append(Integer.toHexString(max));
                max++;
            }
            values.add(newString);
        }
        return ann;
    }

    @Override
    public void printDBStats() {
        //TODO
    }

    @Override
    public boolean isEmpty(Predicate predicate) {
        return !recordCount.containsKey(predicate) || recordCount.get(predicate) == 0;
    }

    @Override
    public Set<Type> findTypes() {
        HashSet<Type> types = new HashSet<>();
        for (Fact fact : facts.values()) {
            types.add(findType(fact, program.tgds));
        }
        return types;
    }

    private Type findType(Fact fact, Set<TGD> tgds) {
        return null;
    }
}
