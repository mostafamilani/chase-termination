package ca.uwo.mmilani.termination.synthetic;

import ca.uwo.mmilani.termination.InvalidOptionException;
import ca.uwo.mmilani.termination.db.Program;
import ca.uwo.mmilani.termination.db.Schema;
import ca.uwo.mmilani.termination.exe.AnalyzerExec;
import ca.uwo.mmilani.termination.exe.DLGPGenerator;
import ca.uwo.mmilani.termination.exe.SyntacticAnalyzer;
import ca.uwo.mmilani.termination.primitives.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProgramGenerator {
    private static final double R_EXIST = 0.1;

    private static void generateRules(Program program, int n_rules, boolean linear) {
        for (int i = 1; i <= n_rules; i++) {
            generateRule(program, linear);
            if (i%500000==0)
                System.out.println(i + " rules are added!");
        }
    }

    private static TGD generateSimpleLinearRule(Program program) {
        TGD tgd = new TGD();
        tgd.body = generateRandomConjunct(program, 1, tgd);
        tgd.head = generateRandomConjunct(program, 1, tgd);

        int n_head_vars = tgd.head.getVariables().size();
        int existVars = randomInRange(new int[]{0, (int) Math.ceil(R_EXIST * n_head_vars)});
        fixExistentialVariables(tgd, existVars);
        program.tgds.add(tgd);
        return tgd;
    }

    private static void generateRule(Program program, boolean linear) {
        TGD tgd = generateSimpleLinearRule(program);
        if (linear) {
            PositiveAtom b = tgd.body.getAtoms().get(0);
            int index = new Random().nextInt(SyntacticAnalyzer.annotationCounts[b.predicate.arity-1]);
            String ant = SyntacticAnalyzer.annotations[index];
            List<Variable> vs = new ArrayList<>(b.getVariables());
            for (int i = 0; i < b.predicate.arity; i++) {
                int j = Integer.parseInt(ant.charAt(i) + "", 16)-1;
                if (j != i) {
                    replaceVarInConjunct(tgd.body, vs.get(i), vs.get(j));
                    replaceVarInConjunct(tgd.head, vs.get(i), vs.get(j));
                }
            }
        }
    }

    private static void fixExistentialVariables(TGD tgd, int nEVars) {
        List<Variable> headVars = tgd.head.getVariables();
        if (headVars.size() < nEVars) return;
        Set<Variable> targetVars = getRandomSubset(headVars, headVars.size() - nEVars);
        for (Variable variable : targetVars) {
            resolveVariable(tgd, variable);
        }
        setExistentialVariables(tgd);
    }

    public static void setExistentialVariables(TGD tgd) {
        for (Variable hVar : tgd.head.getVariables()) {
            if (!tgd.body.getVariables().contains(hVar))
                tgd.existentialVars.add(hVar);
        }
    }

    private static void resolveVariable(TGD tgd, Variable variable) {
        List<Variable> bVars = tgd.body.getVariables();
        Variable bVar = (Variable) getRandomMember(bVars, variable);
        if (bVar == null) return;
        replaceVarInConjunct(tgd.body, variable, bVar);
        replaceVarInConjunct(tgd.head, variable, bVar);
        tgd.variables.remove(variable.toString());
    }

    private static void replaceVarInConjunct(Conjunct conjunct, Variable v1, Variable v2) {
        for (PositiveAtom atom : conjunct.getAtoms()) {
            if (atom.terms.contains(v1))
                atom.terms = replaceVarInTerms(atom.terms, v1, v2);
        }
    }

    private static List<Term> replaceVarInTerms(List<Term> terms, Variable v1, Variable v2) {
        ArrayList<Term> result = new ArrayList<>();
        for (Term term : terms) {
            if (term.equals(v1))
                result.add(v2);
            else
                result.add(term);
        }
        return result;
    }

    public static int randomInRange(int[] range) {
        return ThreadLocalRandom.current().nextInt(range[0], range[1] + 1);
    }

    private static Conjunct generateRandomConjunct(Program program, int nAtoms, TGD tgd) {
        Conjunct conjunct = new Conjunct();
        for (int i = 0; i < nAtoms; i++) {
            ArrayList<Term> terms = new ArrayList<>();
            Predicate predicate = (Predicate) getRandomMember(program.schema.predicates.values());
            for (int j = 0; j < predicate.arity; j++) {
                terms.add(tgd.fetchNewVariable());
            }
            conjunct.add(new PositiveAtom(predicate, terms));
        }
        return conjunct;
    }

    private static Object getRandomMember(Collection set) {
        int size = set.size();
        int item = new Random().nextInt(size);
        int i = 0;
        for (Object obj : set) {
            if (i == item)
                return obj;
            i++;
        }
        return null;
    }

    private static Object getRandomMember(Collection set, Object exclude) {
        if (set.size() == 1 && set.contains(exclude))
            return null;
        Object randomMember = exclude;
        while(exclude.equals(randomMember)) {
            randomMember = getRandomMember(set);
        }
        return randomMember;
    }

    private static Set getRandomSubset(Collection set, int size) {
        HashSet subset = new HashSet();
        while(subset.size() < size) {
            Object randomMember = getRandomMember(set);
            if (subset.contains(randomMember)) continue;
            subset.add(randomMember);
        }
        return subset;
    }

    public static void main(String[] args) {
        try {
            String schemaFile = AnalyzerExec.getOptionValue(args, "-i", false);
            String out = AnalyzerExec.getOptionValue(args, "-o", true);
            int pred = Integer.parseInt(AnalyzerExec.getOptionValue(args, "-p", true));
            int min = Integer.parseInt(AnalyzerExec.getOptionValue(args, "-min", true));
            int max = Integer.parseInt(AnalyzerExec.getOptionValue(args, "-max", true));
            int rules = Integer.parseInt(AnalyzerExec.getOptionValue(args, "-r", true));
            boolean linear = AnalyzerExec.checkOption(args, "-l");

            Program program = new Program();
            if (schemaFile == null) {
                System.out.println("Creating the schema.");
                Schema.createSchema(program, pred, min, max);
            } else {
                System.out.println("Loading the schema.");
                Schema.loadSchema(program, schemaFile);
            }
            System.out.println("Generating the rules.");
            generateProgram(program, out, rules, linear);
        } catch (InvalidOptionException e) {
            System.out.println("A required option is missing: [" + e.option + "]");
        } catch (IOException e) {
            System.out.println("Cannot create the output schema file.");
        } finally {
            System.exit(1);
        }
    }

    private static void generateProgram(Program program, String outfilename, int n_rules, boolean linear) throws IOException {
        program.schema = new Schema();
        generateRules(program, n_rules, linear);
        File outfile = new File(outfilename);
        DLGPGenerator.printProgram(outfile, program);
    }
}
