package ca.uwo.chaseTermination.synthesizer;

import ca.uwo.chaseTermination.engine.Database;
import ca.uwo.chaseTermination.engine.InMemoryDatabase;
import ca.uwo.chaseTermination.engine.Program;
import ca.uwo.chaseTermination.executer.AnalyzerExec;
import ca.uwo.chaseTermination.parser.Parser;
import ca.uwo.chaseTermination.primitives.PositiveAtom;
import ca.uwo.chaseTermination.primitives.TGD;
import ca.uwo.chaseTermination.primitives.Term;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ProgramChecker {
    public static void cleanProgram(String filepath) throws IOException {
        File input = new File(filepath);
        Database database = new InMemoryDatabase();
        Program program = Parser.parseProgram(input, database);

        Map<String, Set<TGD>> partition = new HashMap<>();
        for (TGD tgd : program.tgds) {
            String key = tgd.predicateString();
            if (!partition.containsKey(key))
                partition.put(key, new HashSet<>());
            partition.get(key).add(tgd);
        }

        Set<TGD> confirmedTgds = new HashSet<>();
        for (Set<TGD> ruleSet : partition.values()) {
            Set<TGD> confirmed = new HashSet<>();
            for (TGD tgd : ruleSet) {
                if (!containsTGD(confirmed, tgd)) {
                    confirmed.add(tgd);
                    confirmedTgds.add(tgd);
                }
            }
        }
        System.out.println(program.name +
                " size: " + program.tgds.size() +
                " to " + confirmedTgds.size());
        program.tgds = confirmedTgds;
        String outfilepath = filepath.substring(0, filepath.indexOf(".")) + "-r.txt";
        File outFile = new File(outfilepath);
        outFile.createNewFile();
    }

    private static boolean containsTGD(Set<TGD> tgds, TGD tgd) {
        for (TGD next : tgds) {
            if (equalTGDs(tgd, next)) {
                return true;
            }
        }
        return false;
    }

    private static boolean equalTGDs(TGD t1, TGD t2) {
        PositiveAtom b1 = t1.body.getAtoms().get(0);
        PositiveAtom b2 = t2.body.getAtoms().get(0);
        PositiveAtom h1 = t1.head.getAtoms().get(0);
        PositiveAtom h2 = t2.head.getAtoms().get(0);
        if (!b1.predicate.equals(b2.predicate) || !h1.predicate.equals(h2.predicate))
            return false;
        return existsIsomorphism(b1, b2, h1, h2);
    }

    private static boolean existsIsomorphism(PositiveAtom b1, PositiveAtom b2, PositiveAtom h1, PositiveAtom h2) {
        Map<Term, Term> mapping = new HashMap<>();
        return isomorphic(b1, b2, mapping) &&
                isomorphic(h1, h2, mapping);
    }

    private static boolean isomorphic(PositiveAtom b1, PositiveAtom b2, Map<Term, Term> mapping) {
        for (int i = 0; i < b1.terms.size(); i++) {
            Term tr1 = b1.terms.get(i);
            Term tr2 = b2.terms.get(i);
            if (mapping.containsKey(tr1) &&
                    !mapping.get(tr1).equals(tr2))
                return false;
            if (mapping.containsKey(tr2) &&
                    !mapping.get(tr2).equals(tr1))
                return false;
            mapping.put(tr1, tr2);
        }
        return true;
    }

    public static void main(String[] args) throws IOException, SQLException {
        List files = AnalyzerExec.getFiles("/home/cqadev/Desktop/chase-termination/programs/");
        for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
            File file = (File) iterator.next();
            cleanProgram(file.getAbsolutePath());
        }
    }
}
