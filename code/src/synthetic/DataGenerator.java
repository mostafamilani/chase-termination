package synthetic;

import exceptions.InvalidOptionException;
import db.Schema;
import exe.AnalyzerExec;
import exe.ShapeAnalyzer;
import primitives.Predicate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class DataGenerator {
    private static final int BATCH_SIZE = 20000;

    public static void main(String[] args) {
        String dbname  ="";
        try {
            String user = AnalyzerExec.getOptionValue(args, "-u", true);
            String pass = AnalyzerExec.getOptionValue(args, "-p", true);
            dbname = AnalyzerExec.getOptionValue(args, "-d", true);
            String url = "jdbc:postgresql://localhost/" + dbname + "?user=" + user + "&password=" + pass;
            System.out.println("Connecting to the database " + dbname);
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Creating the database schema");
            createDatabase(conn, args);
            System.out.println("Filling the database");
            fillDatabase(conn, args);
            System.out.println("Printing the database schema in the output file");
            printSchema(conn, args);
            conn.close();
        } catch (InvalidOptionException e) {
            System.out.println("A required option is missing: [" + e.option + "]");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while working with database: [" + dbname + "]");
        } catch (IOException e) {
            System.out.println("Cannot create the output schema file.");
        } finally {
            System.exit(1);
        }
    }

    private static void printSchema(Connection conn, String[] args) throws InvalidOptionException, SQLException, IOException {
        String out = AnalyzerExec.getOptionValue(args, "-o", false);
        if (out == null)
            out = AnalyzerExec.getOptionValue(args, "-d", false) + "-schema.txt";
        File file = new File(out);
        file.createNewFile();
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        Schema schema = Schema.loadSchema(conn, null);
        for (Predicate p : schema.predicates.values()) {
            output.write(p.name + "," + p.arity);
            output.newLine();
        }
        output.close();
    }

    private static void fillDatabase(Connection conn, String[] args) throws SQLException, InvalidOptionException {
        int domainSize = Integer.parseInt(AnalyzerExec.getOptionValue(args, "-dm", true));
        Schema schema = Schema.loadSchema(conn, null);
        fillDatabase(conn, Integer.parseInt(AnalyzerExec.getOptionValue(args, "-t", true)), schema, domainSize);
    }

    private static void createDatabase(Connection conn, String[] args) throws SQLException, InvalidOptionException {
        int[] arityInfo = new int[]{Integer.parseInt(AnalyzerExec.getOptionValue(args, "-min", true)),
                Integer.parseInt(AnalyzerExec.getOptionValue(args, "-max", true))};
        Map<String, Integer> schemaToGenerate = generateSchemaInfo(arityInfo,
                Integer.parseInt(AnalyzerExec.getOptionValue(args, "-r", true)));
        Set<String> ddlQueries = generateDDLQueries(schemaToGenerate);
        createSchema(conn, ddlQueries);
    }

    private static void fillDatabase(Connection conn, int no_records, Schema schema, int domainSize) throws SQLException {
        for (Predicate p : schema.predicates.values()) {
            insertRandomRecords(conn, p, domainSize, no_records);
        }
    }

    private static Set<String> generateDDLQueries(Map<String, Integer> schema) {
        HashSet<String> queries = new HashSet<>();
        for (String relation : schema.keySet()) {
            queries.add(createTableQuery(relation, schema.get(relation)));
        }
        return queries;
    }

    public static String createTableQuery(String tableName, int arity) {
        AtomicReference<StringBuilder> columnDefinitions = new AtomicReference<>(new StringBuilder());
        for (int i = 0; i < arity; i++) {
            columnDefinitions.get().append("c_").append(i).append(" TEXT,");
        }
        columnDefinitions.set(new StringBuilder(columnDefinitions.get().substring(0, columnDefinitions.get().length() - 1)));
        return "create table if not exists \"" + tableName + "\" (" +
                columnDefinitions +
                ");";
    }

    private static Map<String, Integer> generateSchemaInfo(int[] arity, int relations) {
        HashMap<String, Integer> result = new HashMap<>();
        for (int i = 0; i < relations; i++) {
            result.put("P_" + i, ProgramGenerator.randomInRange(arity));
        }
        return result;
    }

    private static void insertRandomRecords(Connection conn, Predicate predicate, int domainSize, int nrecords) throws SQLException {
        StringBuilder query = new StringBuilder("insert into \"" + predicate.name + "\" (");
        for (int j = 0; j < predicate.arity; j++) {
            query.append("c_").append(j).append(",");
        }
        query = new StringBuilder(query.substring(0, query.length() - 1) + ") values ");
        for (int i = 0; i < nrecords; i++) {
            query = getRandomRecordByShape(predicate, domainSize, query);
        }
        query = new StringBuilder(query.substring(0, query.length() - 1) + ";");
        Statement statement = conn.createStatement();
        statement.executeUpdate(query.toString());
        statement.close();
    }

    /*
    * Don't remove! This is an alternative way of generating records and is useful for testing.
    * */
    private static StringBuilder getRandomRecord(Predicate predicate, int domainSize, StringBuilder query) {
        query.append("(");
        for (int j = 0; j < predicate.arity; j++) {
            query.append(ProgramGenerator.randomInRange(new int[]{0, domainSize})).append(",");
        }
        query = new StringBuilder(query.substring(0, query.length() - 1) + "),");
        return query;
    }


    private static StringBuilder getRandomRecordByShape(Predicate predicate, int domainSize, StringBuilder query) {
        String id = ShapeAnalyzer.getRandomId(predicate.arity);
        Map<Character,Integer> values = new HashMap<>();
        query.append("(");
        for (int i = 0; i < id.length(); i++) {
            Character c = id.charAt(i);
            if (!values.containsKey(c)) values.put(c, ProgramGenerator.randomInRange(new int[]{0, domainSize}));
            Integer value = values.get(c);
            query.append(value).append(",");
        }
        query = new StringBuilder(query.substring(0, query.length() - 1) + "),");
        return query;
    }

    private static void createSchema(Connection conn, Set<String> ddlQueries) throws SQLException {
        Statement statement = conn.createStatement();
        for (String query : ddlQueries) {
            statement.addBatch(query);
        }
        statement.executeBatch();
    }
}
