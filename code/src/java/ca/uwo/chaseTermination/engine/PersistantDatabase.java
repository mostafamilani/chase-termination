package ca.uwo.chaseTermination.engine;

import ca.uwo.chaseTermination.executer.OntologyAnalyzer;
import ca.uwo.chaseTermination.primitives.Predicate;
import ca.uwo.chaseTermination.synthesizer.ProgramGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class PersistantDatabase extends Database {
    static String TABLE_PREFIX = "P";
    String schemaName;
    static int INSERT_BATCH_SIZE = 20000;
    private static final Logger log = LogManager.getLogger(PersistantDatabase.class);
    public Connection conn;

    public PersistantDatabase(Properties dbConfig) throws SQLException {
        String user = dbConfig.getProperty("user");
        String pass = dbConfig.getProperty("password");
        String dbname = dbConfig.getProperty("dbname");
        String address = dbConfig.getProperty("url");
        String url = "jdbc:postgresql://" + address + "/" + dbname;
        System.out.println("url = " + url);
        schemaName = dbConfig.getProperty("schema");
        conn = DriverManager.getConnection(url, user, pass);
    }

    public void computeRecordCount() throws SQLException {
        String query = "SELECT relname AS table_name, \n" +
                "       n_live_tup AS row_count\n" +
                "FROM pg_stat_user_tables";
        if (schemaName != null) query += " where schemaname='" + schemaName + "'";
        query += ";";
        try (Statement countStmt = conn.createStatement();
             ResultSet countRs = countStmt.executeQuery(query)) {
            while (countRs.next()) {
                int rowCount = countRs.getInt("row_count");
                String p = countRs.getString("table_name");
                if (schema.predicates.containsKey(p)) {
                    Predicate predicate = schema.predicates.get(p);
                    recordCount.put(predicate, rowCount);
                    predicate.extensional = true;
                }
            }
        }
        /*for (Predicate p : schema.predicates.values()) {
            String countQuery = "SELECT COUNT(*) AS row_count FROM \"" + p.name + "\"";
            log.debug(countQuery);
            try (Statement countStmt = conn.createStatement();
                 ResultSet countRs = countStmt.executeQuery(countQuery)) {
                if (countRs.next()) {
                    int rowCount = countRs.getInt("row_count");
                    recordCount.put(p, rowCount);
                    p.extensional = true;
                }
            }
        }*/
    }

    private static void insertRandomRecords(Connection conn, Predicate predicate, int domainSize, int k) throws SQLException {
        StringBuilder query = new StringBuilder("insert into " + predicate.name + " (");
        for (int j = 0; j < predicate.arity; j++) {
            query.append("c_").append(j).append(",");
        }
        query = new StringBuilder(query.substring(0, query.length() - 1) + ") values ");
        for (int i = 0; i < k; i++) {
            query.append("(");
            for (int j = 0; j < predicate.arity; j++) {
                query.append(ProgramGenerator.randomInRange(new int[]{0, domainSize})).append(",");
            }
            query = new StringBuilder(query.substring(0, query.length() - 1) + "),");
        }
        query = new StringBuilder(query.substring(0, query.length() - 1) + ";");
        Statement statement = conn.createStatement();
        statement.executeUpdate(query.toString());
        statement.close();
    }

    private static String createTableQuery(String tableName, int arity) {
        StringBuilder columnDefinitions = new StringBuilder();
        for (int i = 0; i < arity; i++) {
            columnDefinitions.append("c_").append(i).append(" TEXT,");
        }
        columnDefinitions = new StringBuilder(columnDefinitions.substring(0, columnDefinitions.length() - 1));
        return "create table " + tableName + " (" +
                columnDefinitions +
                ");";
    }

    @Override
    public Map<Predicate, Set<String>> findShapes() throws SQLException {
        Map<Predicate, Set<String>> result = new HashMap<>();
        int n_shapes = 0;
        for (Predicate p : schema.predicates.values()) {
            if (p.extensional) {
                Set<String> shapes = getShapes(p);
                result.put(p, shapes);
                n_shapes += shapes.size();
            }
        }
        program.stats.put(OntologyAnalyzer.NO_DATA_SHAPES, n_shapes);
        return result;
    }

    public Set<String> getShapes(Predicate p) throws SQLException {
        HashSet<String> shapes = new HashSet<>();
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM \"" + p.name + "\"";;
        if (schemaName != null)
            sql = "SELECT * FROM " + schemaName + ".\"" + p.name + "\"";
        log.debug(sql);
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();
        int k = 0;
        while (resultSet.next()) {
            ArrayList<String> values = new ArrayList<>();
            StringBuilder ann = new StringBuilder();
            int max = 1;
            for (int i = 0; i < p.arity; i++) {
                String newString = resultSet.getString("c_" + i);
                if (values.contains(newString))
                    ann.append(Integer.toHexString((values.indexOf(newString) + 1)));
                else {
                    ann.append(Integer.toHexString(max));
                    max++;
                }
                values.add(newString);
            }
            shapes.add(ann.toString());
            k++;
        }
        program.stats.put(OntologyAnalyzer.NO_DATA_SIZE, k);
        return shapes;
    }

    public void fillDatabase(int dmsize, int ntuples) throws SQLException {
        for (Predicate p : schema.predicates.values()) {
            for (int i = 0; i < ntuples; i += INSERT_BATCH_SIZE) {
                insertRandomRecords(conn, p, dmsize, INSERT_BATCH_SIZE);
                log.info("Inserted " + INSERT_BATCH_SIZE + "INSERT_BATCH_SIZE records in " + p.name);
            }
            log.info("Finished table: " + p.name + " #records " + ntuples);
        }
    }

    private Schema createSchema(Set<String> ddlQueries) throws SQLException {
        Statement statement = conn.createStatement();
        for (String query : ddlQueries) {
            statement.addBatch(query);
        }
        statement.executeBatch();
        loadSchema();
        return schema;
    }

    public void createDatabaseSchema(int[] arity, int npre) throws SQLException {
        /* generate schema information */
        Schema schemaToGenerate = Schema.generateRandomSchema(arity, npre);
        Set<String> ddlQueries = generateDDLQueries(schemaToGenerate);

        /* create db schema */
        schema = createSchema(ddlQueries);
        log.info("Created database schema");
    }

    private Set<String> generateDDLQueries(Schema schema) {
        HashSet<String> queries = new HashSet<>();
        for (String relation : schema.predicates.keySet()) {
            queries.add(createTableQuery(relation, schema.predicates.get(relation).arity));
        }
        return queries;
    }

    @Override
    public void printDBStats() {
        System.out.println("Printing information");
        int total = 0;
        for (Predicate predicate : schema.predicates.values()) {
            System.out.println(predicate.name + "\t" + predicate.arity + "\t" + recordCount.get(predicate.name));
            total += recordCount.get(predicate.name);
        }
        System.out.println("Total records = " + total);
    }

    @Override
    public boolean isEmpty(Predicate predicate) {
        return !recordCount.containsKey(predicate) || recordCount.get(predicate) == 0;
    }

    @Override
    public Set<Type> findTypes() {
        return null;
    }

    public Schema loadSchema() throws SQLException {
        schema = new Schema();
        Statement statement = conn.createStatement();
        String query = "SELECT\n" +
                "    CASE\n" +
                "        WHEN table_schema = 'public' THEN table_name\n" +
                "        ELSE table_schema || '.' || table_name\n" +
                "    END AS table_name,\n" +
                "    COUNT(column_name) AS column_count\n" +
                "FROM\n" +
                "    information_schema.columns\n" +
                "WHERE\n" +
                "    table_schema NOT IN ('pg_catalog', 'information_schema') -- Exclude system tables\n" +
                "GROUP BY\n" +
                "    table_schema, table_name\n" +
                "ORDER BY\n" +
                "    table_schema, table_name;\n";
        statement.execute(query);
        ResultSet resultSet = statement.getResultSet();
        while (resultSet.next()) {
            String name = resultSet.getString("table_name");
            int arity = Integer.parseInt(resultSet.getString("column_count"));
            Predicate predicate = schema.fetchPredicate(name, arity);
            schema.predicates.put(name, predicate);
        }
        return schema;
    }
}
