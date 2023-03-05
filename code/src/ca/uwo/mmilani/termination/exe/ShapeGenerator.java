package ca.uwo.mmilani.termination.exe;

import ca.uwo.mmilani.termination.db.Schema;
import ca.uwo.mmilani.termination.primitives.Predicate;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ShapeGenerator {
    public static void main(String[] args) throws SQLException, IOException {
        Map<Predicate, Set<String>> result = new HashMap<>();
        Properties prop = new Properties();
        prop.load(new FileInputStream("/home/cqadev/IdeaProjects/omdfinal/src/db-config.properties"));
        String user = prop.get("user").toString();
        String pass = prop.get("password").toString();
//        String dbname = "LUBM01k";
//        String dbname = "Ontology-256";
        String dbname = "STB-128";
//        String dbname = "deep";
        String url = "jdbc:postgresql://localhost/" + dbname + "?user=" + user + "&password=" + pass;
        Connection conn = DriverManager.getConnection(url, user, pass);
        long time = System.nanoTime();
        Schema schema = Schema.loadSchema(conn);
        System.out.println("Time to load schema = " + (System.nanoTime() - time) / 1000000F);
        System.out.println("Number of predicates = " + schema.predicates.values().size());
        time = System.nanoTime();
        int count = 0;
        Statement st = conn.createStatement();
        for (Predicate predicate : schema.predicates.values()) {
            count += getShapes(predicate, st, -1).size();
        }
        System.out.println("Number of shapes = " + count);
        System.out.println("Time to compute shapes = " + (System.nanoTime() - time) / 1000000F);
        st.close();
    }

    public static Set<String> getShapes(Predicate p, Statement st, int dbsize) throws SQLException {
        HashSet<String> shapes = new HashSet<>();
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
                        boolean b = checkEquality(p, newColumns, st, dbsize);
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
