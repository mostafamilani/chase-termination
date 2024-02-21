package ca.uwo.chaseTermination.executer;

import ca.uwo.chaseTermination.engine.Database;
import ca.uwo.chaseTermination.engine.InMemoryDatabase;
import ca.uwo.chaseTermination.engine.PersistantDatabase;
import ca.uwo.chaseTermination.engine.Program;
import ca.uwo.chaseTermination.graph.Edge;
import ca.uwo.chaseTermination.graph.FCCNode;
import ca.uwo.chaseTermination.graph.FCComponent;
import ca.uwo.chaseTermination.graph.Node;
import ca.uwo.chaseTermination.parser.Parser;
import ca.uwo.chaseTermination.primitives.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.uwo.chaseTermination.executer.SyntacticAnalyzer.*;
import static ca.uwo.chaseTermination.executer.TerminationAnalyzer.terminatesLinear;

public class OntologyAnalyzer {

    public static final String RESULT_FILE_EXTENSION = ".res";
    public static final String AVG_ARITY = "avg_arity";
    public static final String MIN_ARITY = "min_arity";
    public static final String MAX_ARITY = "max_arity";
    public static final String NO_EVARS = "n_exist_vars";
    public static final String TIME_GENERATE_DEP_GRAPH = "t_graph";
    public static final String TIME_PARSING = "t_parse";
    public static final String TIME_CONNECTED_COMPONENT = "t_component";
    public static final String TIME_TERMINATES_GRAPH = "t_terminate";
    public static final String TIME_TERMINATES_CHASE = "t_lunatic";
    public static final String TIME_FIND_SHAPES = "t_shapes";
    public static final String NO_RULES = "n_rules";
    public static final String NO_PREDICATES = "n_predicates";
    public static final String NO_DATA_SIZE = "n_facts";
    public static final String NO_DATA_SHAPES = "n_shapes";
    public static final String NO_SPECIAL_CONNECTED_COMPONENTS = "n_spacial_components";
    public static final String NO_GRAPH_NODES = "n_nodes";
    public static final String NO_GRAPH_EDGES = "n_edges";
    public static final String NO_GRAPH_SPECIAL_EDGES = "n_special_edges";
    public static final String TERMINATES_GRAPH = "terminates";

    private static final Logger log = LogManager.getLogger(OntologyAnalyzer.class);

    public static void exportResults(String filename, Map<String, Object> result) throws IOException {
        File outfile = new File(filename);
        outfile.createNewFile();
        FileWriter out = new FileWriter(outfile);
        List<String> keys = new ArrayList<>(result.keySet());
        Collections.sort(keys);
        for (String key : keys)
            out.write(key + ": " + result.get(key) + "\n");
        out.close();
    }
    private static String[] findDirectoryAndOntologyName(String filePath) {
        String[] result = new String[3];
        Pattern pattern = Pattern.compile("[\\\\/]");
        Matcher matcher = pattern.matcher(filePath);
        int lastIndex = -1;
        while (matcher.find())
            lastIndex = matcher.start();
        result[0] = filePath.substring(0, lastIndex + 1);
        result[1] = filePath.substring(lastIndex + 1, filePath.lastIndexOf("."));
        result[2] = result[0] + result[1] + RESULT_FILE_EXTENSION;
        ;
        return result;
    }

    private static Map<Predicate, Map<String, Set<TGD>>> constructIndexForShapes(Program program) {
        Map<Predicate, Map<String, Set<TGD>>> shapeRules = new HashMap<>();
        for (TGD tgd : program.tgds) {
            Predicate p = tgd.body.getAtoms().get(0).predicate;
            String shape = generateShape(tgd);
            if (!shapeRules.containsKey(p)) shapeRules.put(p, new HashMap<>());
            if (!shapeRules.get(p).containsKey(shape)) shapeRules.get(p).put(shape, new HashSet<>());
            shapeRules.get(p).get(shape).add(tgd);
        }
        return shapeRules;
    }

    private static Map<Position, Node> buildLinearDependencyGraph(Map<Predicate, Set<String>> deltaShapes,
                                                                  Map<Predicate, Map<String, Set<TGD>>> shapeRules,
                                                                  Program newProgram, Program program) {
        Map<Position, Node> linearGraph = new HashMap<>();
        Set<String> fired = new HashSet<>();
        Map<Predicate, Set<String>> newShapes = new HashMap<>();
        while (!deltaShapes.isEmpty()) {
            for (Predicate p : deltaShapes.keySet()) {
                Set<String> newFired = new HashSet<>();
                if (!shapeRules.keySet().contains(p))
                    continue;
                for (String shape : deltaShapes.get(p)) {
                    newFired.add(shape + "@" + p);
                    for (String ruleShape : shapeRules.get(p).keySet()) {
                        if (!compatible(ruleShape, shape))
                            continue;
                        for (TGD tgd : shapeRules.get(p).get(ruleShape)) {
                            if (!fired.contains(shape + "@" + p)) {
                                TGD sTGD = simplify(newProgram, tgd, shape);
                                SyntacticAnalyzer.updateGraph(linearGraph, sTGD);
                                Predicate hAnn = sTGD.head.getAtoms().get(0).predicate;
                                String headAnnotation = hAnn.name.substring(hAnn.name.indexOf("@") + 1);
                                Predicate h = program.database.schema.predicates.get(hAnn.name.substring(0, hAnn.name.indexOf("@")));
                                if ((!deltaShapes.containsKey(h) || !deltaShapes.get(h).contains(headAnnotation)) && !fired.contains(headAnnotation + "@" + h)) {
                                    if (!newShapes.containsKey(h)) {
                                        newShapes.put(h, new HashSet<>());
                                    }
                                    newShapes.get(h).add(headAnnotation);
                                }
                            }
                        }
                    }
                }
                fired.addAll(newFired);
            }
            deltaShapes = new HashMap<>(newShapes);
            newShapes = new HashMap<>();
        }
        return linearGraph;
    }
    private static void computeAdditionalStats(Program program) {
        int sum = 0, count = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Predicate predicate : program.database.schema.predicates.values()) {
            sum += predicate.arity;
            count++;
            min = Math.min(predicate.arity, min);
            max = Math.max(predicate.arity, max);
        }
        program.stats.put(AVG_ARITY, (double) sum / (double) count);
        program.stats.put(NO_PREDICATES, count);
        program.stats.put(MIN_ARITY, min);
        program.stats.put(MAX_ARITY, max);
    }

    private static void analyzeProgramTermination(Program program, boolean simple) throws SQLException {
        long startTime, endTime;

        Map<Position, Node> graph;
        Program newProgram = program;
        if (simple) {
            log.info("Building dependency graph...");
            startTime = System.nanoTime();
            graph = buildDependencyGraph(program.tgds);
            endTime = System.nanoTime();
            program.stats.put(OntologyAnalyzer.TIME_GENERATE_DEP_GRAPH, (endTime - startTime) / 1000000F);
        } else {
            log.info("Finding db shapes...");
            startTime = System.nanoTime();
            Map<Predicate, Set<String>> deltaShapes = program.database.findShapes();
            Map<Predicate, Map<String, Set<TGD>>> shapeRules = constructIndexForShapes(program);
            endTime = System.nanoTime();
            program.stats.put(OntologyAnalyzer.TIME_FIND_SHAPES, (endTime - startTime) / 1000000F);

            log.info("Building dependency graph for simplified program...");
            startTime = System.nanoTime();
            newProgram = new Program();
            newProgram.database = new InMemoryDatabase();
            graph = buildLinearDependencyGraph(deltaShapes, shapeRules, newProgram, program);
            endTime = (System.nanoTime());
            program.stats.put(OntologyAnalyzer.TIME_GENERATE_DEP_GRAPH, (endTime - startTime) / 1000000F);
        }
        program.stats.put(NO_RULES, program.tgds.size());
        program.stats.put(NO_DATA_SIZE, Database.getDBSize(program));
        program.stats.put(NO_EVARS, computeTotalExistsVars(program));
        addGraphStats(program, graph);


        log.info("Finding fully connected components and checking termination...");
        startTime = System.nanoTime();
        Set<FCComponent> fullyConnectedComponents = findFullyConnectedComponents(graph);
        Set<Node> nodesInSpecialCycle = getNodesInSpecialCycle(fullyConnectedComponents, program);
        boolean terminates;
        if (simple) {
            Set<Position> loopPositions = getPositions(nodesInSpecialCycle);
            List<FCCNode> fullyConnectedComponentsGraph = createComponentGraph(fullyConnectedComponents, program.database);
            terminates = TerminationAnalyzer.terminatesSimpleLinear(fullyConnectedComponentsGraph, loopPositions, program);
        } else {
            Set<Node> nodes = SyntacticAnalyzer.getNodesInSpecialCycle(fullyConnectedComponents, newProgram);
            terminates = terminatesLinear(nodes);
        }
        endTime = System.nanoTime();
        program.stats.put(OntologyAnalyzer.TIME_CONNECTED_COMPONENT, (endTime - startTime) / 1000000F);

        program.stats.put(OntologyAnalyzer.TERMINATES_GRAPH, terminates);
        computeAdditionalStats(program);
    }

    private static void addGraphStats(Program program, Map<Position, Node> graph) {
        program.stats.put(OntologyAnalyzer.NO_GRAPH_NODES, graph.keySet().size());
        int edges = 0, eEdges = 0;
        for (Position p : graph.keySet()) {
            edges += graph.get(p).nextEdges.size();
            for (Edge edge : graph.get(p).nextEdges) {
                if (edge.special) eEdges++;
            }
        }
        program.stats.put(OntologyAnalyzer.NO_GRAPH_EDGES, edges);
        program.stats.put(OntologyAnalyzer.NO_GRAPH_SPECIAL_EDGES, eEdges);
    }

    private static boolean compatible(String s1, String s2) {
        if (s1.length() != s2.length())
            return false;
        Set<int[]> pairs = new HashSet<>();
        for (int i = 0; i < s1.length(); i++) {
            char a = s1.charAt(i);
            for (int j = i + 1; j < s1.length(); j++) {
                char b = s1.charAt(j);
                if (a == b)
                    pairs.add(new int[]{i, j});
            }
        }
        for (int[] pair : pairs) {
            if (s2.charAt(pair[0]) != s2.charAt(pair[1]))
                return false;
        }
        return true;
    }

    private static String generateShape(TGD tgd) {
        PositiveAtom a = tgd.body.getAtoms().get(0);
        String[] vars = new String[a.predicate.arity];
        StringBuilder ann = new StringBuilder();
        int max = 1;
        int i = 0;
        for (Term term : a.terms) {
            vars[i] = term.toString();
            boolean repeated = false;
            int j;
            for (j = 0; j < i; j++) {
                if (vars[j].equals(vars[i])) {
                    repeated = true;
                    break;
                }
            }
            if (repeated) {
                ann.append(j + 1);
            } else {
                ann.append(max);
                max++;
            }
            i++;
        }
        return ann.toString();
    }

    public static int computeTotalExistsVars(Program program) {
        AtomicInteger count = new AtomicInteger();
        program.tgds.forEach(tgd -> count.addAndGet(tgd.existentialVars.size()));
        return count.get();
    }

    private static boolean hasDataForChecking(Program program) {
        for (TGD tgd : program.tgds) {
            PositiveAtom b = tgd.body.getAtoms().get(0);
            if (!program.database.isEmpty(b.predicate))
                return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        log.info("Running Ontology Analyzer...");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            System.out.println("arg = " + arg);
        }
        String filePath = AnalyzerExec.getOptionValueWithAssert(args, "-f", 1);

        log.info("File path is " + filePath);
        String[] res = findDirectoryAndOntologyName(filePath);
        String ontologyName = res[1];
        String resultFileName = res[2];

        if (AnalyzerExec.checkOption(args, "-o"))
            resultFileName = AnalyzerExec.getOptionValue(args, "-o", 1);
        log.info("Results will be saved to: " + resultFileName);

        Database database;
        if (AnalyzerExec.checkOption(args, "-d")) {
            Properties dbConfig = new Properties();
            dbConfig.load(new FileInputStream(AnalyzerExec.getOptionValueWithAssert(args, "-d", 1)));
            database = new PersistantDatabase(dbConfig);
        } else {
            database = new InMemoryDatabase();
        }
        long endTime, startTime;
        startTime = System.nanoTime();
        Program program = Parser.parseProgram(new File(filePath), database);
        endTime = System.nanoTime();
        program.stats.put(TIME_PARSING, (endTime - startTime) / 1000000F);

        if (database instanceof PersistantDatabase) {
            ((PersistantDatabase)database).computeRecordCount();
            if (!hasDataForChecking(program)) {
                log.info("The database does not have relevant data.");
                System.exit(1);
            }
        }

        boolean simple = !AnalyzerExec.checkOption(args, "-l");

        if (!program.isLinear()) {
            if (AnalyzerExec.checkOption(args,"-ln"))
                program.removeNonLinearRules();
            else {
                log.error(ontologyName + " is not linear.\n\n");
                System.exit(1);
            }
        }

        if (simple && !program.isSimpleLinear()) {
            log.error(ontologyName + " is not simple-linear.\n\n");
            System.exit(1);
        }

        if (program.tgds.isEmpty()) {
            log.info(ontologyName + " has no rules, and trivially terminates!\n\n");
            System.exit(0);
        }


        log.info("Starting graph-based analysis for " + ontologyName);
        analyzeProgramTermination(program, simple);

        log.info("Graph-based analysis finished.");

        exportResults(resultFileName, program.stats);
        log.info("Processing " + ontologyName + " completed!\n\n");
    }
}
