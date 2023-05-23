package exe;

import db.Program;
import db.Schema;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.io.ParseException;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import org.semanticweb.rulewerk.client.picocli.RulewerkClientMaterialize;
import org.semanticweb.rulewerk.core.reasoner.KnowledgeBase;
import org.semanticweb.rulewerk.core.reasoner.Reasoner;
import org.semanticweb.rulewerk.examples.ExamplesUtils;
import org.semanticweb.rulewerk.graal.GraalConjunctiveQueryToRule;
import org.semanticweb.rulewerk.graal.GraalToRulewerkModelConverter;
import org.semanticweb.rulewerk.reasoner.vlog.VLogReasoner;
import parsing.Parser;
import primitives.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import static exe.SyntacticAnalyzer.buildDependencyGraph;

public class VLogChecker {
    public static void main(String[] args) throws SQLException, IOException {
        String dbname = "deep";
//		String dbname = "LUBM100";
        String user = "postgres";
        String pass = "admin";
        String url = "jdbc:postgresql://localhost/" + dbname + "?user=" + user + "&password=" + pass + "&stringtype=unspecified&standard_conforming_strings=off";
        Connection connection = DriverManager.getConnection(url, user, pass);
        String path = "C:\\Users\\mmilani7\\Downloads\\real-ont\\";
        Program program = Parser.parseProgram(new File(path + "deep_100.txt"));
        Schema schema = Schema.loadSchema(connection, program);
        System.out.println("schema.predicates.values().size() = " + schema.predicates.values().size());
        for (Predicate p : schema.predicates.values()) {
            Set<Fact> facts = loadFacts(p, connection);
            for (Fact fact : facts) {
                program.edb.facts.add(fact);
            }
        }
        System.out.println("program.edb.facts.size() = " + program.edb.facts.size());
        long endTime, startTime;
        startTime = System.nanoTime();
        checkTermination(program, path);
        endTime = System.nanoTime();
        float time = (endTime - startTime) / 1000000F;
        System.out.println("time = " + time);
    }

    private static Set<Fact> loadFacts(Predicate p, Connection connection) throws SQLException {
        HashSet<Fact> facts = new HashSet<>();
        Statement statement = connection.createStatement();
        String columns = "";
        for (int i = 0; i < p.arity; i++) {
            columns += "c_" + i + ",";
        }
        columns = columns.substring(0, columns.length()-1);
        String sql = "select * from " + p.name + " order by " + columns + ";";
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();
        while(resultSet.next()) {
            ArrayList<Term> terms = new ArrayList<>();
            for (int i = 0; i < p.arity; i++) {
                String s = resultSet.getString("c_" + i);
                terms.add(new Constant(s));
            }
            facts.add(new Fact(p, terms));
        }
        return facts;
    }

    public static void printProgram(File outFile, Program program, boolean printParameters) throws IOException {
        FileWriter out = new FileWriter(outFile);
        out.write("@rules\n");
        for (TGD tgd : program.tgds) {
            out.write(tgd.toString().replaceAll(":-", " :- ") + ".\n");
        }

        out.write("@facts\n");
        if (program.edb != null) {
            for (Fact fact : program.edb.facts) {
                out.write(toString(fact) + ".\n");
            }
        }

//        @parameters
        if (printParameters) {
            for (String key : program.externalParams.keySet()) {
                out.write("@parameters " + key + "=" + program.externalParams.get(key) + "\n");
            }
        }
        out.close();
    }

    private static String toString(Fact fact) {
        String s = fact.predicate.name + "(";
        for (Term term : fact.terms) {
            String c = term.toString();
            c = c.replace(".", "");
            c = c.replace("X", "c");
            if (c.equals("")) c = "c";
            if (c.matches("^\\d.*")) c = "c" + c;
            s += "\"" + c + "\",";
        }
        return s.substring(0, s.length()-1) + ")";
    }

    public static boolean checkTermination(Program program, String path) throws IOException {
        final List<Atom> graalAtoms = new ArrayList<>();
        final List<fr.lirmm.graphik.graal.api.core.Rule> graalRules = new ArrayList<>();
        final List<ConjunctiveQuery> graalConjunctiveQueries = new ArrayList<>();

        if (program.edb.facts.size() < 1) {
            program.stats.put(OntologyAnalyzer.TIME_TERMINATES_VLOG, -2);
            return true;
        }
        File file = new File(path + "dlgp");
        file.createNewFile();
        printProgram(file, program, false);
        DlgpParser parser = new DlgpParser(file);
        while (parser.hasNext()) {
            final Object object = parser.next();
            if (object instanceof Atom) {
                graalAtoms.add((Atom) object);
            } else if (object instanceof Rule) {
                graalRules.add((Rule) object);
            } else if (object instanceof ConjunctiveQuery) {
                graalConjunctiveQueries.add((ConjunctiveQuery) object);
            }
        }
        final List<GraalConjunctiveQueryToRule> convertedConjunctiveQueries = new ArrayList<>();

        for (final ConjunctiveQuery conjunctiveQuery : graalConjunctiveQueries) {
            final String queryUniqueId = "query" + convertedConjunctiveQueries.size();
            convertedConjunctiveQueries.add(GraalToRulewerkModelConverter.convertQuery(queryUniqueId, conjunctiveQuery));
        }


        KnowledgeBase kb = new KnowledgeBase();
        Reasoner reasoner = new VLogReasoner(kb);
        kb.addStatements(GraalToRulewerkModelConverter.convertAtomsToFacts(graalAtoms));
        kb.addStatements(GraalToRulewerkModelConverter.convertRules(graalRules));
        boolean mfa = false;
        reasoner.isMFC();
        file.delete();
        return mfa;
    }
}
