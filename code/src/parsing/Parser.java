package parsing;

import db.Program;
import db.Database;
import db.Schema;
import primitives.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static Program parseProgram(File file) throws IOException {
        Program program = new Program();
        program.edb = new Database();
        program.edb.program = program;
        program.schema = new Schema();
        program.schema.program = program;
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) continue;
            if (line.startsWith("@parameters")) {
                loadExternalParameter(program, line);
            }
            if (line.startsWith("@facts") || line.startsWith("@rules"))
                continue;
            if (line.replaceAll(
                    " ", "").equals("") || line.contains("?") || line.contains("%")) continue;
            if (line.equals("Prefixes: [") || line.equals("Statistics: [")) line = skipToNextLine(reader);
            if (line == null) continue;
            if (line.equals("Deterministic DL-clauses: [") ||
                    line.equals("Disjunctive DL-clauses: [") ||
                    line.equals("ABox: [") ||
                    line.equals("]")) continue;
            if (line.contains(" v ")) line = line.replaceAll(" v ", ",");
            if (line.contains("=")) continue;
            if (line.replaceAll(" ", "").startsWith(":-")) continue;
            if (line.contains("atLeast(") && line.contains(":-")) {
                line = cleanRule(line);
            }
            try {
                if (line.contains(":-")) {
                    Rule rule = parseRule(line, program);
                    rule.addProgram(program);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String completeName = file.getAbsolutePath();
        program.name = completeName.substring(completeName.lastIndexOf("\\")+1, completeName.lastIndexOf("."));
        return program;
    }

    private static void loadExternalParameter(Program program, String line) {
        line = line.substring("@parameters".length()).replaceAll(" ", "");
        program.externalParams.put(line.substring(0, line.indexOf("=")), line.substring(line.indexOf("=")+1));
    }

    private static String cleanRule(String line) {
//        System.out.println("line = " + line);
//        atLeast(1 acgt:reveals acgt:TotalBilirubin)(X) :- acgt:TotalBilirubinTest(X)
//        acgt:reveals(X,ZZ),acgt:TotalBilirubin(X):- acgt:TotalBilirubinTest(X)
        String body = line.substring(line.indexOf(":-"));;
        StringTokenizer t0 = new StringTokenizer(line.substring(0, line.indexOf(":-")), ",");
        String head = "";
        while(t0.hasMoreTokens()) {
            String token = t0.nextToken();
            if (!token.contains("atLeast")) {
                head += token + ",";
            } else {
                StringTokenizer t = new StringTokenizer(token, " ()\n\t");
                t.nextToken();
                t.nextToken();
                String p = t.nextToken().replaceAll(" ", "");
                String q = t.nextToken().replaceAll(" ", "");
                if (q.equals("{")) {
                    String var = token.substring(token.indexOf("}")).replaceAll(" ", "");
                    String x = var.substring(3, var.length()-1);
                    head += p + "(" + x + ",ZZ),";
                } else if (q.equals("not")) {
                    t.nextToken();
                    String x = t.nextToken().replaceAll(" ", "");
                    head += p + "(" + x + ",ZZ),";
                } else {
                    String x = t.nextToken().replaceAll(" ", "");
                    head += p + "(" + x + ",ZZ)," + q + "(" + x + "),";
                }
            }
        }
        return head.substring(0, head.length()-1) + body;
    }

    private static String skipToNextLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("]")) return reader.readLine();
        }
        return line;
    }

    private static Rule parseRule(String line, Program program) {
//        System.out.println("line = " + line);
        String head = "";
//        line = line.replaceAll(" ", "");
        Rule rule;
        if (line.startsWith(":-")) {
            rule = new NC();
        } else if (line.contains("=")) {
            rule = new EGD();
        } else {
            rule = new TGD();
        }
        rule.body = parseConjunct(line.substring(line.indexOf(":-") + 2), true, program, rule);
        if (rule instanceof  TGD) {
            TGD tgd = (TGD) rule;
            rule.head = parseConjunct(line.substring(0, line.indexOf(":-")), false, program, rule);
            tgd.existentialVars = new HashSet<>(tgd.head.getVariables());
            tgd.existentialVars.removeAll(tgd.body.getVariables());
        } else if (rule instanceof EGD) {
            head = line.substring(0, line.indexOf(":-"));
            rule.head = parse(head, false, program);
        }
        return rule;
    }

    public static Conjunct parseConjunct(String s, boolean body, Program program, Rule... rule) {
        Conjunct conjunct = new Conjunct();
        StringTokenizer tokenizer = new StringTokenizer(s, " \t\r(", true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken(" \t\r(");
            if (token.equals(".")) continue;
            if (!token.startsWith("!") && !token.equals(" ")) {
                if (token.startsWith(",")) token = token.substring(1);
                String atomStr = token;
                atomStr += tokenizer.nextToken(")");
                atomStr += tokenizer.nextToken();
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
            if (line.contains("%")) continue;
            if (line.contains("?")) {
                queries.add(parseQuery(line, program));
            }
        }
        return queries;

    }

    private static CQ parseQuery(String line, Program program) {
        CQ query = new CQ();
        line = line.replaceAll(
                " ", "");
        String body = line.substring(line.indexOf("?-") + 2, line.length()-1);
        String head = line.substring(0, line.indexOf("?-"));
        query.body = parseConjunct(body, true, program);
        query.head = parse(head, false, program);
        return query;
    }

    public static String sanitizePredicateName(String name) {
        String res = name.replaceAll("-|:|!|\\.|#|<|>|/|=|\\+|~|`|'|\\{|}|\"|\\^| ", "");
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(res);
        while (m.find()) {
            String text = m.group(1);
            res.replace(text, text.replaceAll(",", ""));
        }
        return res;
    }

    public static Atom parse(String s, boolean body, Program program, Rule... owner) {
        StringTokenizer t = new StringTokenizer(s.replace(" ", ""), "(,=)");
        Atom atom = null;
        if (s.contains("=")) {
            atom = new EqualityAtom(Term.parse(t.nextToken(), body, program), Term.parse(t.nextToken(), body, program));
        } else {
            String pname = t.nextToken();
            List<Term> ts = new ArrayList<>();
            while (t.hasMoreTokens()) {
                Term term = Term.parse(t.nextToken(), body, program, owner);
                ts.add(term);
            }
            Predicate p = program.schema.fetchPredicate(pname, ts.size());
            atom = new PositiveAtom(p, ts);
        }
        return atom;
    }

    public static Map<String, String> readParameters(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String s1,s2,s3;
        s1 = in.readLine();
        s2 = in.readLine();
        s3 = in.readLine();
        String s = in.readLine();
        HashMap<String, String> result = new HashMap<>();
        while(s != null) {
            s1 = s2;
            s2 = s3;
            s3 = s;
            s = in.readLine();
        }
        extracted(s1, result);
        extracted(s2, result);
        extracted(s3, result);
        return result;
    }

    private static void extracted(String s, HashMap<String, String> result) {
        s = s.substring("@parameters".length()).replaceAll(" ", "");
        result.put(s.substring(0, s.indexOf("=")), s.substring(s.indexOf("=")+1));
    }
}
