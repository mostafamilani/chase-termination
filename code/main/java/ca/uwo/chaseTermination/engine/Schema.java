package ca.uwo.chaseTermination.engine;

import ca.uwo.chaseTermination.parser.Parser;
import ca.uwo.chaseTermination.primitives.Constant;
import ca.uwo.chaseTermination.primitives.Predicate;
import ca.uwo.chaseTermination.synthesizer.ProgramGenerator;

import java.util.HashMap;
import java.util.Map;

public class Schema {

    public Map<String, Constant> constants = new HashMap<>();
    public Map<String, Predicate> predicates = new HashMap<>();
    public Program program;

    public static Schema generateRandomSchema(int[] arity, int npre) {
        Schema schema = new Schema();
        Map<String, Integer> schemaToGenerate = new HashMap<>();
        HashMap<String, Integer> result = new HashMap<>();
        for (int i = 0; i < npre; i++) {
            String name = PersistantDatabase.TABLE_PREFIX + (i + 1);
            schema.predicates.put(name, new Predicate(name, ProgramGenerator.randomInRange(arity)));
        }
        return schema;
    }


    public Constant fetchConstant(String label) {
        if (constants.containsKey(label))
            return constants.get(label);

        Constant newConstant = new Constant(label);
        constants.put(label, newConstant);
        return newConstant;
    }

    public Predicate fetchPredicate(String name, int arity) {
        name = Parser.sanitizePredicateName(name);
        Predicate predicate;
        if (!predicates.containsKey(name)) {
            predicate = new Predicate(name, arity, program);
            predicates.put(name, predicate);
        } else {
            predicate = predicates.get(name);
            if (predicate.arity != arity)
                throw new RuntimeException("Invalid Arity! " + predicate);
        }
        return predicate;
    }

    public Predicate fetchSimplePredicate(Predicate predicate) {
        if (!predicate.isAdorned())
            return null;
        return fetchPredicate(predicate.name.substring(0, predicate.name.indexOf("^")), predicate.arity);
    }

    public Predicate fetchMagicPredicate(Predicate predicate) {
        if (!predicate.isAdorned())
            return null;
        Predicate p = fetchSimplePredicate(predicate);

        String adornment = predicate.getAdornment();
        return fetchPredicate("m_" + p.name + "^" + adornment, adornment.replaceAll("b", "").length());
    }
}
