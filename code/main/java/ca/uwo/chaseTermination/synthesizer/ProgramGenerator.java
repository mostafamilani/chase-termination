package ca.uwo.chaseTermination.synthesizer;

import ca.uwo.chaseTermination.engine.*;
import ca.uwo.chaseTermination.executer.AnalyzerExec;
import ca.uwo.chaseTermination.executer.SyntacticAnalyzer;
import ca.uwo.chaseTermination.primitives.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProgramGenerator {
    private static final double EXIST_RATIO = 0.1;

    private static void generateRules(Program program, int n_rules, double r_exist) {
        System.out.println("The number of rules: " + n_rules);
        for (int i = 1; i <= n_rules; i++) {
            generateLinearRule(program, r_exist, true);
            if (i % 500000 == 0)
                System.out.println(i + " rules are added!");
        }
    }

    private static TGD generateSimpleLinearRule(Program program, double r_exist) {
        TGD tgd = new TGD();
        tgd.body = generateRandomConjunct(program, 1, tgd);
        tgd.head = generateRandomConjunct(program, 1, tgd);

        int n_head_vars = tgd.head.getVariables().size();
        int existVars = randomInRange(new int[]{0, (int) Math.ceil(r_exist * n_head_vars)});
        fixExistentialVariables(tgd, existVars);
        program.tgds.add(tgd);
        return tgd;
    }

    private static void generateLinearRule(Program program, double r_exist, boolean policy) {
        TGD tgd = generateSimpleLinearRule(program, r_exist);
        PositiveAtom b = tgd.body.getAtoms().get(0);
        if (policy) {
            int index = new Random().nextInt(SyntacticAnalyzer.annotationCounts[b.predicate.arity - 1]);
            String ant = SyntacticAnalyzer.annotations[index];
            List<Variable> vs = new ArrayList<>(b.getVariables());
            for (int i = 0; i < b.predicate.arity; i++) {
                int j = Integer.parseInt(ant.charAt(i) + "", 16) - 1;
                if (j != i) {
                    replaceVarInConjunct(tgd.body, vs.get(i), vs.get(j));
                    replaceVarInConjunct(tgd.head, vs.get(i), vs.get(j));
                }
            }
        } else {
            List<Variable> vars = b.getVariables();
            b.terms = new ArrayList<>();
            for (int i = 0; i < vars.size(); i++) {
                b.terms.add(vars.get(new Random().nextInt(vars.size())));
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
            Predicate predicate = (Predicate) getRandomMember(program.database.schema.predicates.values());
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
        while (exclude.equals(randomMember)) {
            randomMember = getRandomMember(set);
        }
        return randomMember;
    }

    private static Set getRandomSubset(Collection set, int size) {
        HashSet subset = new HashSet();
        while (subset.size() < size) {
            Object randomMember = getRandomMember(set);
            if (subset.contains(randomMember)) continue;
            subset.add(randomMember);
        }
        return subset;
    }

    private static Program generateProgram(Database database, String outfilename, int nrules) throws IOException {
        Program program = new Program();
        program.database = database;
        generateRules(program, nrules, ProgramGenerator.EXIST_RATIO);

        File outfile = new File(outfilename);
        if (!outfile.exists()) outfile.createNewFile();
        FileWriter writer = new FileWriter(outfile);
        for (TGD tgd : program.tgds) {
            writer.write(tgd.toString() + "\n");
        }
        writer.close();
        return program;
    }

    public static void main(String[] args) throws IOException, SQLException {
        String config = AnalyzerExec.getOptionValue(args, "-c", 1);
        Database database;
        if (config != null) {
            Properties prop = new Properties();
            prop.load(new FileInputStream(config));
            database = new PersistantDatabase(prop);
            ((PersistantDatabase)database).loadSchema();
            ((PersistantDatabase)database).computeRecordCount();
        } else {
            int npredic = Integer.parseInt(AnalyzerExec.getOptionValueWithAssert(args, "-pr", 1));
            int[] arity = new int[2];
            arity[0] = Integer.parseInt(AnalyzerExec.getOptionValueWithAssert(args, "-min", 1));
            arity[1] = Integer.parseInt(AnalyzerExec.getOptionValueWithAssert(args, "-max", 1));
            database = new InMemoryDatabase();
            database.schema = Schema.generateRandomSchema(arity, npredic);
        }
        int nrules = Integer.parseInt(AnalyzerExec.getOptionValueWithAssert(args, "-r", 1));

        System.out.println("Program with n_predicate " + database.schema.predicates.keySet().size() +
                ", n_rule " + nrules);
        String out = AnalyzerExec.getOptionValue(args, "-o", 1);
        if (out == null) out = "program.txt";
        generateProgram(database, out, nrules);
    }
}
