package exe;

import exceptions.InvalidOptionException;
import db.Program;
import parsing.Parser;
import primitives.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static exe.SyntacticAnalyzer.*;

public class OntologyAnalyzer {
    public static final String AVG_ARITY = "avg_arity";
    public static final String MIN_ARITY = "min_arity";
    public static final String MAX_ARITY = "max_arity";
    public static final String NO_EVARS = "n_exist_vars";
    public static final String TIME_GENERATE_DEP_GRAPH = "t_graph";
    public static final String TIME_GENERATE_DEP_GRAPH_D = "t_graph_d";
    public static final String TIME_PARSING = "t_parse";
    public static final String TIME_CONNECTED_COMPONENT = "t_component";
    public static final String TIME_TERMINATES_GRAPH = "t_terminate_graph";
    public static final String TIME_TERMINATES_CHASE = "t_vlog";
    public static final String TIME_FIND_SHAPES = "t_shapes_m";
    public static final String TIME_FIND_SHAPES_DB = "t_shapes_db";
    public static final String TIME_TERMINATES_GRAPH_D = "t_terminate_graph_d";
    public static final String NO_RULES = "n_rules";
    public static final String NO_SIMPLE_RULES_D = "n_simplified_rules_d";
    public static final String NO_SIMPLE_RULES = "n_simplified_rules";
    public static final String NO_PREDICATES = "n_predicates";
    public static final String NO_DATA_SIZE = "n_facts";
    public static final String NO_DATA_SHAPES = "n_shapes";
    public static final String NO_CONNECTED_COMPONENTS = "n_components";
    public static final String NO_SPECIAL_CONNECTED_COMPONENTS = "n_spacial_components";
    public static final String NO_GRAPH_NODES = "n_nodes";
    public static final String NO_GRAPH_EDGES = "n_edges";
    public static final String NO_GRAPH_SPECIAL_EDGES = "n_special_edges";
    public static final String NO_CONNECTED_COMPONENTS_D = "n_components_d";
    public static final String NO_SPECIAL_CONNECTED_COMPONENTS_D = "n_spacial_components_d";
    public static final String NO_GRAPH_NODES_D = "n_nodes_d";
    public static final String NO_GRAPH_EDGES_D = "n_edges_d";
    public static final String NO_GRAPH_SPECIAL_EDGES_D = "n_special_edges_d";
    public static final String TERMINATES = "terminates";

    public static void main(String[] args) {
        try {
            String input = AnalyzerExec.getOptionValue(args, "-f", true);
            String output = AnalyzerExec.getOptionValue(args, "-o", true);

            long endTime, startTime = System.nanoTime();
            File inputFile = new File(input);
            Program program = Parser.parseProgram(inputFile);

            program.stats.putAll(program.externalParams);
            program.stats.put(OntologyAnalyzer.NO_DATA_SIZE, program.edb.facts.size());
            endTime = System.nanoTime();
            program.stats.put(TIME_PARSING, (endTime - startTime) / 1000000F);

            if (AnalyzerExec.checkOption(args, "-v")) {
                startTime = System.nanoTime();
                boolean terminates = VLogChecker.checkTermination(Parser.parseProgram(inputFile), ".");
                endTime = System.nanoTime();
                program.stats.put(TIME_TERMINATES_CHASE, (endTime - startTime) / 1000000F);
                program.stats.put(TERMINATES, terminates);
            } else {
                processSyntax(program, args);
            }

            exportResults(output, program, AnalyzerExec.checkOption(args, "-a"));
            System.out.println("Checking is complete!");
        } catch (InvalidOptionException e) {
            System.out.println("A required option is missing: [" + e.option + "]");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while working with database.");
        } catch (IOException e) {
            System.out.println("Cannot create the output schema file.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(1);
        }
    }

    public static void exportResults(String filename, Program program, boolean append) throws IOException {
        File outfile = new File(filename);
        if (!outfile.exists()) {
            boolean created = outfile.createNewFile();
            if (!created) throw new IOException("Couldn't create the output file.");
        }
        FileWriter out = new FileWriter(outfile, append);
        List<String> keys = new ArrayList<>(program.stats.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            if (!append || key.equals(TIME_TERMINATES_GRAPH))
                out.write(key + ": " + program.stats.get(key) + "\n");
        }
        out.close();
    }

    public static void processSyntax(Program program, String[] args) throws SQLException, InvalidOptionException, IOException {
            long endTime, startTime;
            startTime = System.nanoTime();
            System.out.println("Building the dependency graph.");
            program.depGraph = buildDependencyGraph(program);
            endTime = System.nanoTime();
            float t_graph = (endTime - startTime) / 1000000F;
            program.stats.put(TIME_GENERATE_DEP_GRAPH, t_graph);

            startTime = System.nanoTime();
            System.out.println("Finding special fully connected components.");
            findSpecialFCCs(program);
            endTime = System.nanoTime();
            float t_components = (endTime - startTime) / 1000000F;
            program.stats.put(TIME_CONNECTED_COMPONENT, t_components);
            startTime = System.nanoTime();
            System.out.println("Check for supported positions in the special fully connected components.");
            if (AnalyzerExec.checkOption(args, "-d")) {
                String user = AnalyzerExec.getOptionValue(args, "-u", true);
                String pass = AnalyzerExec.getOptionValue(args, "-p", true);
                String dbname = AnalyzerExec.getOptionValue(args, "-d", true);
                String tuples = AnalyzerExec.getOptionValue(args, "-t", false);
                String schema = AnalyzerExec.getOptionValue(args, "-s", false);
                if (tuples != null) program.edb.limit = Integer.parseInt(tuples);
                String url = "jdbc:postgresql://localhost/" + dbname + "?user=" + user + "&password=" + pass + "&stringtype=unspecified&standard_conforming_strings=off";
                System.out.println("Connecting to the database " + dbname);
                program.edb.conn = DriverManager.getConnection(url, user, pass);
                if (schema != null) {
                    program.edb.conn.setSchema(schema);
                    program.edb.schemaName = schema;
                }
                program.eclass = Program.LINEAR;
                program.removeNonLinearTGDs();
            }
            boolean terminates;
            if (program.eclass == Program.SIMPLE_LINEAR)
                terminates = TerminationAnalyzer.terminates(program);
            else if (program.eclass == Program.LINEAR) {
                terminates = TerminationAnalyzer.terminatesLinear(program, args);
            } else {
                System.out.println("The set of rules is not linear.");
                return;
            }

            endTime = System.nanoTime();
            program.stats.put(TIME_TERMINATES_GRAPH, ((endTime - startTime) / 1000000F) + t_graph + t_components);
            program.stats.put(NO_RULES, program.tgds.size());
//            program.stats.put(NO_DATA_SIZE, computeDBSize(program));
            program.stats.put(NO_GRAPH_NODES, program.depGraph.keySet().size());
            program.stats.put(NO_GRAPH_EDGES, program.dpEdges);
            program.stats.put(NO_GRAPH_SPECIAL_EDGES, program.dpEsEdges);
            program.stats.put(NO_CONNECTED_COMPONENTS, program.nComponents);
            program.stats.put(NO_SPECIAL_CONNECTED_COMPONENTS, program.nSpecialComponents);
            program.stats.put(NO_EVARS, program.nExistential);
            program.stats.put(TERMINATES, terminates);
            computeArityInfo(program);
    }

    public static int computeDBSize(Program program) {
        int size = 0;
        for (Integer count : program.edb.recordCount.values()) {
            size += count;
        }
        return size;
    }

    public static void computeArityInfo(Program program) {
        double sum = 0, count = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Predicate predicate : program.schema.predicates.values()) {
            sum += predicate.arity;
            count++;
            min = Math.min(predicate.arity, min);
            max = Math.max(predicate.arity, max);
        }
        program.stats.put(AVG_ARITY, sum/count);
        program.stats.put(NO_PREDICATES, count);
        program.stats.put(MIN_ARITY, min);
        program.stats.put(MAX_ARITY, max);
    }
}
