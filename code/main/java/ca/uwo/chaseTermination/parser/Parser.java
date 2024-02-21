package ca.uwo.chaseTermination.parser;

import ca.uwo.chaseTermination.engine.Database;
import ca.uwo.chaseTermination.engine.InMemoryDatabase;
import ca.uwo.chaseTermination.engine.Program;
import ca.uwo.chaseTermination.primitives.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final Logger log = LogManager.getLogger(Parser.class);

    public static Program parseProgram(File file, Database database) throws IOException {
        log.info("Parsing file: " + file.getAbsolutePath());
        Program program = new Program();
        program.database = database;
        parseFile(file, program);
        String completeName = file.getAbsolutePath();
        program.name = completeName.substring(completeName.lastIndexOf("\\") + 1, completeName.lastIndexOf("."));
        program.database.program = program;
        log.info("Parsing completed.");
        return program;
    }

    private static void parseFile(File file, Program program) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while ((line = reader.readLine()) != null) {
            if (ignoreLine(line))
                continue;
            line = skipToNextLine(reader, line);
            line = disjunctionToConjunction(line);
            addRulesOrFactsToProgram(program, line);
        }
    }

    private static boolean ignoreLine(String line) {
        return lineIsEmpty(line) ||
                LineIsCommentOrEGD(line) ||
                lineIsClauseOrDescribingDB(line) ||
                lineIsAnEmptyRule(line) ||
                lineIsFactsOrRulesAnnotations(line);
    }

    private static boolean LineIsCommentOrEGD(String line) {
        return line.startsWith("#") ||
                line.contains("?") ||
                line.contains("%") ||
                line.contains("=");
    }

    private static boolean lineIsClauseOrDescribingDB(String line) {
        return line.equals("Deterministic DL-clauses: [") ||
                line.equals("Disjunctive DL-clauses: [") ||
                line.equals("ABox: [") ||
                line.equals("]");
    }

    private static boolean lineIsAnEmptyRule(String line) {
        return line.replaceAll(" ", "").startsWith(":-");
    }

    private static boolean lineIsFactsOrRulesAnnotations(String line) {
        return line.startsWith("@facts") ||
                line.startsWith("@rules");
    }

    private static boolean lineIsEmpty(String line) {
        return line == null || line.replaceAll(" ", "").isEmpty();
    }

    private static String disjunctionToConjunction(String line) {
        if (line.contains(" v "))
            return line.replaceAll(" v ", ",");
        return line;
    }

    private static void addRulesOrFactsToProgram(Program program, String line) {
        if (line.contains(":-")) {
            if (line.contains("atLeast("))
                line = cleanRule(line);
            Rule rule = parseRule(line, program);
            program.addRule(rule);
        } else
            ((InMemoryDatabase) program.database).addFact((Fact) parse(sanitizePredicateName(line), false, program));
    }

    private static String cleanRule(String line) {
        String body = line.substring(line.indexOf(":-"));
        StringTokenizer t0 = new StringTokenizer(line.substring(0, line.indexOf(":-")), ",");
        StringBuilder head = new StringBuilder();
        while (t0.hasMoreTokens()) {
            String token = t0.nextToken();
            if (!token.contains("atLeast"))
                head.append(token).append(",");
            else
                cleanToken(token, head);
        }
        return head.substring(0, head.length() - 1) + body;
    }

    private static void cleanToken(String token, StringBuilder head) {
        StringTokenizer t = new StringTokenizer(token, " ()\n\t");
        t.nextToken();
        t.nextToken();
        String p = t.nextToken().replaceAll(" ", "");
        String q = t.nextToken().replaceAll(" ", "");
        if (q.equals("{")) {
            String var = token.substring(token.indexOf("}")).replaceAll(" ", "");
            String x = var.substring(3, var.length() - 1);
            head.append(p).append("(").append(x).append(",ZZ),");
        }
        else if (q.equals("not")) {
            t.nextToken();
            String x = t.nextToken().replaceAll(" ", "");
            head.append(p).append("(").append(x).append(",ZZ),");
        }
        else {
            String x = t.nextToken().replaceAll(" ", "");
            head.append(p).append("(").append(x).append(",ZZ),").append(q).append("(").append(x).append("),");
        }
    }

    private static String skipToNextLine(BufferedReader reader, String line) throws IOException {
        if (line.equals("Prefixes: [") || line.equals("Statistics: [")) {
            String newLine;
            while ((newLine = reader.readLine()) != null)
                if (newLine.equals("]")) return reader.readLine();
            return newLine;
        }
        return line;
    }

    private static Rule parseRule(String line, Program program) {
        Rule rule = createRuleBasedOnLineFormat(line);
        rule.body = parseConjunct(line.substring(line.indexOf(":-") + 2), true, program, rule);
        rule = processTGDRule(rule, line, program);
        rule = processEGDRule(rule, line, program);
        return rule;
    }

    private static Rule createRuleBasedOnLineFormat(String line) {
        if (line.startsWith(":-"))
            return new NC();
        else if (line.contains("="))
            return new EGD();
        return new TGD();
    }

    private static Rule processTGDRule(Rule rule, String line, Program program) {
        if (rule instanceof TGD tgd) {
            tgd.head = parseConjunct(line.substring(0, line.indexOf(":-")), false, program, tgd);
            tgd.existentialVars = new HashSet<>(tgd.head.getVariables());
            tgd.body.getVariables().forEach(tgd.existentialVars::remove);
            program.tgds.add(tgd);
            return tgd;
        }
        return rule;
    }

    private static Rule processEGDRule(Rule rule, String line, Program program) {
        if (rule instanceof EGD) {
            String head = line.substring(0, line.indexOf(":-"));
            rule.head = parse(head, false, program);
        }
        return rule;
    }

    public static Conjunct parseConjunct(String s, boolean body, Program program, Rule... rule) {
        Conjunct conjunct = new Conjunct();
        StringTokenizer tokenizer = new StringTokenizer(s, " \t\r(", true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken(" \t\r(");
            if (token.equals("."))
                continue;
            if (!token.startsWith("!") && !token.equals(" ")) {
                if (token.startsWith(","))
                    token = token.substring(1);
                String atomStr = token +
                        tokenizer.nextToken(")") +
                        tokenizer.nextToken();
                conjunct.add((PositiveAtom) parse(atomStr, body, program, rule));
            }
        }
        return conjunct;
    }

    public static List<CQ> parseQueries(File file, Program program) throws IOException {
        ArrayList<CQ> queries = new ArrayList<>();
        String line;
        BufferedReader in = new BufferedReader(new FileReader(file));
        while ((line = in.readLine()) != null) {
            if (line.contains("%"))
                continue;
            if (line.contains("?"))
                queries.add(parseQuery(line, program));
        }
        return queries;

    }

    private static CQ parseQuery(String line, Program program) {
        CQ query = new CQ();
        line = line.replaceAll(" ", "");
        String body = line.substring(line.indexOf("?-") + 2, line.length() - 1);
        String head = line.substring(0, line.indexOf("?-"));
        query.body = parseConjunct(body, true, program);
        query.head = parse(head, false, program);
        return query;
    }

    public static String sanitizePredicateName(String name) {
        String result = name.replaceAll("-|:|!|\\.|#|<|>|/|=|\\+|~|`|'|\\{|}|\"|\\^| ", "");
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(result);
        while (matcher.find()) {
            String text = matcher.group(1);
            result.replace(text, text.replaceAll(",", ""));
        }
        return result;
    }

    public static Atom parse(String s, boolean body, Program program, Rule... owner) {
        StringTokenizer tokenizer = new StringTokenizer(s.replace(" ", ""), "(,=)");
        Atom atom;
        if (s.contains("="))
            atom = new EqualityAtom(Term.parse(tokenizer.nextToken(), body, program), Term.parse(tokenizer.nextToken(), body, program));
        else {
            String pname = tokenizer.nextToken();
            List<Term> ts = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                Term term = Term.parse(tokenizer.nextToken(), body, program, owner);
                ts.add(term);
            }
            Predicate p = program.database.schema.fetchPredicate(pname, ts.size());
            if (Atom.isFact(ts)) {
                atom = ((InMemoryDatabase) program.database).addFact(p, ts);
            } else
                atom = new PositiveAtom(p, ts);
        }
        return atom;
    }
}
