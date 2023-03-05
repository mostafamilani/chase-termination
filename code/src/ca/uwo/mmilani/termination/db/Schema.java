package ca.uwo.mmilani.termination.db;

import ca.uwo.mmilani.termination.parsing.Parser;
import ca.uwo.mmilani.termination.primitives.Constant;
import ca.uwo.mmilani.termination.primitives.Predicate;
import ca.uwo.mmilani.termination.synthetic.ProgramGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Schema {
    public Map<String, Constant> constants = new HashMap<>();
    public Map<String, Predicate> predicates = new HashMap<>();
    public Program program;

    public static Schema loadSchema(Connection conn) throws SQLException {
        Schema schema = new Schema();
        Statement statement = conn.createStatement();
        statement.execute("select c.relname as table_name, c.reltuples as reltuples, c.relnatts as relnatts\n" +
                "from pg_class c\n" +
                "join pg_namespace n on n.oid = c.relnamespace\n" +
                "where c.relkind = 'r'\n" +
                "      and n.nspname not in ('information_schema','pg_catalog')\n" +
                "order by table_name;");
        ResultSet resultSet = statement.getResultSet();
        while(resultSet.next()) {
            String name = resultSet.getString("table_name");
            int arity = Integer.parseInt(resultSet.getString("relnatts"));
            Predicate predicate = schema.fetchPredicate(name, arity);
            schema.predicates.put(name, predicate);
        }
        return schema;
    }

    public static void loadSchema(Program program, String schema) throws IOException {
        program.schema = new Schema();
        HashMap<String, Predicate> predicates = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(schema));
        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
            StringTokenizer t = new StringTokenizer(line, ",");
            String pname = t.nextToken();
            String arity = t.nextToken();
            predicates.put(pname, new Predicate(pname, Integer.parseInt(arity), program));
        }
        reader.close();
        program.schema.predicates = predicates;
        program.schema.program = program;
    }

    public static void createSchema(Program program, int predCount, int minArity, int maxArity) {
        program.schema = new Schema();
        HashMap<String, Predicate> predicates = new HashMap<>();
        for (int i = 1; i <= predCount; i++) {
            int[] range = new int[]{minArity, maxArity};
            String name = "p_" + i;
            predicates.put(name, new Predicate(name, ProgramGenerator.randomInRange(range), program));
        }
        program.schema.predicates = predicates;
        program.schema.program = program;
    }

    public Constant fetchConstant(String label) {
        if (!constants.containsKey(label))
            constants.put(label, new Constant(label));
        return constants.get(label);
    }

    public Predicate fetchPredicate(String name, int arity) {
        name = Parser.sanitizePredicateName(name);
        if (!predicates.containsKey(name))
            predicates.put(name, new Predicate(name, arity, program));
        Predicate predicate = predicates.get(name);
        if (predicate.arity != arity)
            throw new RuntimeException("Invalid Arity! " + predicate);
        return predicate;
    }

    public int maxArity() {
        int max = Integer.MIN_VALUE;
        for (Predicate predicate : predicates.values()) {
            if (predicate.arity > max)
                max = predicate.arity;
        }
        return max;
    }

    public Predicate fetchAdornedPredicate(Predicate predicate, String adornment) {
        if (predicate.isAdorned())
            return null;
        return fetchPredicate(predicate.name + "^" + adornment, predicate.arity);
    }

    public Predicate fetchSimplePredicate(Predicate predicate) {
        if (!predicate.isAdorned())
            return null;
        return fetchPredicate(predicate.name.substring(0, predicate.name.indexOf("^")), predicate.arity);
    }

    public Set<Predicate> allAdorned() {
        HashSet<Predicate> result = new HashSet<>();
        for (Predicate predicate : predicates.values()) {
            if (predicate.name.contains(predicate.name + "^")) {
                result.add(predicate);
            }
        }
        return result;
    }

    public Predicate fetchMagicPredicate(Predicate predicate) {
        if (!predicate.isAdorned())
            return null;
        Predicate p = fetchSimplePredicate(predicate);

        String adornment = predicate.getAdornment();
        return fetchPredicate("m_" + p.name + "^" + adornment, adornment.replaceAll("b", "").length());
    }
}
