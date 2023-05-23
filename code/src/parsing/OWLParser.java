package parsing;

import db.Program;
import primitives.Fact;
import primitives.Predicate;
import primitives.Term;
import synthetic.DataGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.*;
import java.util.Iterator;

public class OWLParser {
    private static final String ONTOLOGIES_PATH = "http://krr-nas.cs.ox.ac.uk/ontologies/UID/";

    public static void main(String[] args) throws IOException {
        /*generating batch file for downloading OWL files using HermiT*/
        /*String path = "C:\\Users\\mmilani7\\Downloads\\owl-ont\\";
        generateBatch(path);*/

        String path = "C:\\Users\\mmilani7\\Downloads\\owl-ont\\";
        File dir = new File(path);
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".txt"));
        for (File file : files) {
            System.out.println(file.getName());
            Program program = Parser.parseProgram(file);
            persistEDB(program);
        }
        /*Program p = Parser.parseProgram(new File("C:\\Users\\mmilani7\\Downloads\\owl-ont\\00008-ht.txt"));
        System.out.println("p = " + p);
        persistEDB(p);*/
    }

    private static void persistEDB(Program program) {
        if (program.edb.facts == null || program.edb.facts.size() == 0)
            return;
        String url = "jdbc:postgresql://localhost:5432/";
        String username = "postgres";
        String password = "admin";
        String dbName = "owl-ont";


        try {
            Connection conn = DriverManager.getConnection(url + dbName + "?user=" + username + "&password=" + password);
            Statement stmt = conn.createStatement();
            String schema = "sch" + program.name.replace("-", "");
            String sql = "SELECT 1 FROM pg_namespace WHERE nspname = '" + schema + "'";
//            System.out.println("sql = " + sql);
            ResultSet rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                sql = "CREATE SCHEMA " + schema + ";";
//                System.out.println("sql = " + sql);
                stmt.executeUpdate(sql);
                System.out.println("Database created!");
                conn.setSchema(schema);
                createSchema(program, stmt);
                System.out.println("Schema created!");
                insertTuples(program, stmt);
                System.out.println("Database Instance created!");
            } else {
                System.out.println("Database already exists!");
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error creating database: " + e.getMessage());
        }
    }

    private static void insertTuples(Program program, Statement stmt) throws SQLException {
        String q;
        int i = 0;
        for (Fact fact : program.edb.facts) {
            try {
                q = getInsertQuery(fact);
//            System.out.println("q = " + q);
                stmt.executeUpdate(q);
                i++;
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Could not insert fact:" + fact);
            }
        }
        System.out.println("Number of tuples: " + i);
    }

    private static String getInsertQuery(Fact fact) {
        Predicate predicate = fact.predicate;
        StringBuilder query = new StringBuilder("insert into \"" + predicate.name + "\" (");
        for (int j = 0; j < predicate.arity; j++) {
            query.append("c_").append(j).append(",");
        }
        query = new StringBuilder(query.substring(0, query.length() - 1) + ") values (");
        for (Term term : fact.terms) {
            query.append("'" + term + "'").append(",");
        }
        query = new StringBuilder(query.substring(0, query.length() - 1) + ");");
        return String.valueOf(query);
    }

    private static void createSchema(Program program, Statement stmt) throws SQLException {
        for (Predicate p : program.edb.edbSchema) {
            String q = DataGenerator.createTableQuery(p.name, p.arity);
            stmt.executeUpdate(q);
        }
        System.out.println("Number of tables: " + program.edb.edbSchema.size());
    }
}
