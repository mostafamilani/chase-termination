package exe;

import db.Program;
import db.Schema;
import primitives.Predicate;
import synthetic.ProgramGenerator;

import java.io.*;
import java.sql.*;
import java.util.*;

public class ShapeAnalyzer {

    private static Set<String> findShapes(String dbname, Program program, int size) throws IOException, SQLException {
        HashSet<String> result = new HashSet<>();
        Properties prop = new Properties();
        prop.load(new FileInputStream("/home/cqadev/IdeaProjects/omdfinal/src/db-config.properties"));
        String user = prop.get("user").toString();
        String pass = prop.get("password").toString();
        String url = "jdbc:postgresql://localhost/" + dbname + "?user=" + user + "&password=" + pass;
        Connection conn = DriverManager.getConnection(url, user, pass);
        Collection<Predicate> predicates = Schema.loadSchema(conn, program).predicates.values();
        for (Predicate p : predicates) {
            System.out.println("Get shapes for predicate " + p);
            result.addAll(getShapes(p, conn, program, size));
        }
        conn.close();
        return result;
    }

    public static String getRandomId(int arity) {
        return SyntacticAnalyzer.annotations[ProgramGenerator.randomInRange(new int[]{1,SyntacticAnalyzer.annotationCounts[arity]})].substring(0,arity);
    }

    public static Map<Predicate, Set<String>> findShapes(Set<Predicate> predicates, Program program, int size, Connection conn, boolean query) throws SQLException {
        Map<Predicate, Set<String>> result = new HashMap<>();
        Schema extSchema = Schema.loadSchema(conn, program);
        int n_shapes = 0;
        for (Predicate p : predicates) {
            if (!extSchema.predicates.containsKey(p.name)) {
                continue;
            }
            if (query) {
                result.put(p, getShapesQuery(p, conn, program, size));
            } else {
                result.put(p, getShapes(p, conn, program, size));
            }
            n_shapes += result.get(p).size();
        }
        conn.close();
        program.stats.put(OntologyAnalyzer.NO_DATA_SHAPES, n_shapes);
        return result;
    }

    private static Set<String> getShapes(Predicate p, Connection conn, Program program, int size) throws SQLException {
        HashSet<String> shapes = new HashSet<>();
        if (p.arity == 1) {
            shapes.add("1");
            return shapes;
        }
        Statement statement = conn.createStatement();
        String columns = "";
        for (int i = 0; i < p.arity; i++) {
            columns += "c_" + i + ",";
        }
        columns = columns.substring(0, columns.length()-1);
        String sql = "select * from \"" + p.name + "\" order by " + columns + " limit " + size + ";";
        System.out.println("sql = " + sql);
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();
        int k=0;
        while(resultSet.next()) {
            String[] values = new String[p.arity];
            StringBuilder ann = new StringBuilder();
            int max = 1;
            for (int i = 0; i < p.arity; i++) {
                values[i] = resultSet.getString("c_" + i);
                boolean repeated = false;
                int j = 0;
                for (j = 0; j < i; j++) {
                    if (values[j].equals(values[i])) {
                        repeated = true;
                        break;
                    }
                }
                if (repeated) {
                    char c = ann.charAt(j);
                    ann.append(c);
                } else {
                    ann.append(Integer.toHexString(max));
                    max++;
                }
            }
            shapes.add(ann.toString());
            k++;
        }
        if (program.stats.containsKey(OntologyAnalyzer.NO_DATA_SIZE)) {
            program.stats.put(OntologyAnalyzer.NO_DATA_SIZE, ((int) program.stats.get(OntologyAnalyzer.NO_DATA_SIZE)) + k);
        }
        return shapes;
    }

    public static Set<String> getShapesQuery(Predicate p, Connection conn, Program program, int size) throws SQLException {
        HashSet<String> shapes = new HashSet<>();
        if (p.arity == 1) {
            shapes.add("1");
            return shapes;
        }
        Statement st = conn.createStatement();
        int qcount = 0;
//        System.out.println(p.name + " with arity " + p.arity);
        if (p.arity <= 1) return shapes;
        Set<String> columns = new HashSet<>();
        Set<Set<String>> columnSets = new HashSet<>();
        for (int i = 0; i < p.arity; i++) {
            String c = "c_" + i;
            columns.add(c);
            Set<String> singleton = new HashSet<>();
            singleton.add(c);
            columnSets.add(singleton);
        }
        Set<Set<String>> newColumnSets = new HashSet<>();
        Set<Set<String>> checkedColumnSets = new HashSet<>();
        while (!columnSets.isEmpty()) {
            for (Set<String> columnSet : columnSets) {
                for (String c : columns) {
                    Set<String> newColumns = new HashSet<>(columnSet);
                    if (!newColumns.contains(c)) {
                        newColumns.add(c);
                        if (checkedColumnSets.contains(newColumns)) continue;
                        boolean b = checkEquality(p, newColumns, st, size);
                        qcount++;
                        if (b) {
                            newColumnSets.add(newColumns);
                            shapes.add(generateShape(p, newColumns));
                        }
                        checkedColumnSets.add(newColumns);
                    }
                }
            }
            columnSets = newColumnSets;
            newColumnSets = new HashSet<>();
        }
        System.out.println("#queries:" + qcount);
        return shapes;
    }

    private static String generateShape(Predicate p, Set<String> columns) {
        return null;
    }

    private static boolean checkEquality(Predicate p, Set<String> columns, Statement statement, int dbsize) throws SQLException {
        String conditions = "     ";
        String c1 = null;
        for (String c : columns) {
            if (c1 == null)
                c1 = c;
            else
                conditions += c1 + "=" + c + " AND ";
        }
        conditions = conditions.substring(0, conditions.length() - 4);
//        String q = "SELECT 1\n" +
//                " WHERE EXISTS( SELECT 1\n" +
//                "                 FROM " + p.name +
//                "                WHERE " + conditions + " )";
//        System.out.println("q = " + q);

        String limit = " LIMIT " + dbsize;
        if (dbsize < 0)
            limit = "";
        String q = "SELECT 1\n" +
                " WHERE EXISTS( SELECT 1\n" +
                "                 FROM (SELECT * FROM " + p.name + limit + ") AS Recs" +
                "                WHERE " + conditions + " )";
//        System.out.println("q = " + q);
        statement.execute(q);
        ResultSet resultSet = statement.getResultSet();
        return resultSet.next();
    }
}
