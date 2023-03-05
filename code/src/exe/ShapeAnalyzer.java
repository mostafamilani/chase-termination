package exe;

import db.Program;
import db.Schema;
import primitives.Predicate;

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
        Collection<Predicate> predicates = Schema.loadSchema(conn).predicates.values();
        for (Predicate p : predicates) {
            System.out.println("Get shapes for predicate " + p);
            result.addAll(getShapes(p, conn, program, size));
        }
        conn.close();
        return result;
    }

    public static Map<Predicate, Set<String>> findShapes(Set<Predicate> predicates, Program program, int size, Connection conn) throws SQLException {
        Map<Predicate, Set<String>> result = new HashMap<>();
        Schema extSchema = Schema.loadSchema(conn);
        program.stats.put(OntologyAnalyzer.NO_DATA_SIZE, 0);
        int n_shapes = 0;
        for (Predicate p : predicates) {
            if (!extSchema.predicates.containsKey(p.name)) {
                continue;
            }
            result.put(p, getShapes(p, conn, program, size));
            n_shapes += result.get(p).size();
        }
        conn.close();
        program.stats.put(OntologyAnalyzer.NO_DATA_SHAPES, n_shapes);
        return result;
    }

    private static Set<String> getShapes(Predicate p, Connection conn, Program program, int size) throws SQLException {
        HashSet<String> shapes = new HashSet<>();
        Statement statement = conn.createStatement();
        String columns = "";
        for (int i = 0; i < p.arity; i++) {
            columns += "c_" + i + ",";
        }
        columns = columns.substring(0, columns.length()-1);
        String sql = "select * from " + p.name + " order by " + columns + " limit " + size + ";";
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
}
