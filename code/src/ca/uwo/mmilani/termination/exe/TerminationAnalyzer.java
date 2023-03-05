package ca.uwo.mmilani.termination.exe;

import ca.uwo.mmilani.termination.InvalidOptionException;
import ca.uwo.mmilani.termination.db.Program;
import ca.uwo.mmilani.termination.primitives.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static ca.uwo.mmilani.termination.exe.SyntacticAnalyzer.*;

public class TerminationAnalyzer {
    public static boolean terminates(Program program) {
        if (program.sfccs.isEmpty()) {
            return true;
        }
        if (program.edb == null)
            return true;
        Set<Node> badNodes = new HashSet<>();
        Set<Node> seeds = new HashSet<>();
        for (FCComponent component : program.sfccs) {
            boolean seedAdded = false;
            for (Node node : component.members) {
                badNodes.add(node);
                if (program.edb.isEmpty(node.p.predicate)) return false;
                if (!seedAdded) seeds.add(node);
            }
        }
        for (Node seed : seeds) {
            if (reverseDepthFirst(seed, badNodes, program)) return false;
        }
        return true;
    }

    private static boolean reverseDepthFirst(Node node, Set<Node> visited, Program program) {
        Set<Node> nodes = node.nextNodes(true);
        for (Node next : nodes) {
            if (visited.contains(next)) continue;
            if (program.edb.isEmpty(node.p.predicate)) return true;
            if (reverseDepthFirst(node, visited, program)) return true;
            visited.add(next);
        }
        return false;
    }

    public static boolean terminatesLinear(Program program, String[] args) throws IOException, SQLException, InvalidOptionException {
        Set<Predicate> predicates = new HashSet<>(program.schema.predicates.values());

        String user = AnalyzerExec.getOptionValue(args, "-u", true);
        String pass = AnalyzerExec.getOptionValue(args, "-p", true);
        String dbname = AnalyzerExec.getOptionValue(args, "-d", true);
        int tuples = Integer.parseInt(AnalyzerExec.getOptionValue(args, "-t", true));
        String url = "jdbc:postgresql://localhost/" + dbname + "?user=" + user + "&password=" + pass;
        System.out.println("Connecting to the database " + dbname);
        Connection conn = DriverManager.getConnection(url, user, pass);

        long startTime = System.nanoTime();
        Map<Predicate, Set<String>> deltaShapes = ShapeAnalyzer.findShapes(predicates, program, tuples, conn);
        long endTime = System.nanoTime();
        float t_shapes = (endTime - startTime) / 1000000F;
        program.stats.put(OntologyAnalyzer.TIME_FIND_SHAPES, t_shapes);


        startTime = System.nanoTime();
        Map<Predicate, Map<String, Set<TGD>>>  shapeRules = new HashMap<>();
        for (TGD tgd : program.tgds) {
            Predicate p = tgd.body.getAtoms().get(0).predicate;
            String shape = generateShape(tgd);
            if (!shapeRules.containsKey(p)) shapeRules.put(p, new HashMap<>());
            if (!shapeRules.get(p).containsKey(shape)) shapeRules.get(p).put(shape, new HashSet<>());
            shapeRules.get(p).get(shape).add(tgd);
        }

        Map<Position, Node> graph = new HashMap<>();
        Set<String> fired = new HashSet<>();
        Program newProgram = new Program();
        Map<Predicate, Set<String>> newShapes = new HashMap<>();
        while(!deltaShapes.isEmpty()) {
            for (Predicate p : deltaShapes.keySet()) {
                Set<String> newFired = new HashSet<>();
                if (!shapeRules.containsKey(p)) {
                    continue;
                }
                for (String shape : deltaShapes.get(p)) {
                    newFired.add(shape + "@" + p);
                    for (String ruleShape : shapeRules.get(p).keySet()) {
                        if (!compatible(ruleShape, shape))  continue;
                        for (TGD tgd : shapeRules.get(p).get(ruleShape)) {
                            if (!fired.contains(shape + "@" + p)) {
                                TGD sTGD = simplify(newProgram, tgd, shape);
                                if (sTGD == null) continue;
                                SyntacticAnalyzer.updateGraph(graph, sTGD, program);
                                Predicate hAnn = sTGD.head.getAtoms().get(0).predicate;
                                String headAnnotation = hAnn.name.substring(hAnn.name.indexOf("@") + 1);
                                Predicate h = program.schema.predicates.get(hAnn.name.substring(0, hAnn.name.indexOf("@")));
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
        endTime = System.nanoTime();
        float t_graph_d = (endTime - startTime) / 1000000F;
        program.stats.put(OntologyAnalyzer.TIME_GENERATE_DEP_GRAPH_D, t_graph_d);
        program.stats.put(OntologyAnalyzer.NO_SIMPLE_RULES_D, newProgram.tgds.size());
        program.stats.put(OntologyAnalyzer.NO_SIMPLE_RULES, simplifyCount(program));
        startTime = System.nanoTime();
        SyntacticAnalyzer.findSpecialFCCs(newProgram);
        endTime = System.nanoTime();
        float t_components = (endTime - startTime) / 1000000F;
        program.stats.put(OntologyAnalyzer.TIME_CONNECTED_COMPONENT, t_components);
        program.stats.put(OntologyAnalyzer.TIME_TERMINATES_GRAPH_D, (t_components+t_graph_d+t_shapes));
        program.stats.put(OntologyAnalyzer.NO_GRAPH_NODES_D, graph.keySet().size());
        program.stats.put(OntologyAnalyzer.NO_GRAPH_EDGES_D, program.dpEdges);
        program.stats.put(OntologyAnalyzer.NO_GRAPH_SPECIAL_EDGES_D, program.dpEsEdges);
        program.stats.put(OntologyAnalyzer.NO_CONNECTED_COMPONENTS_D, newProgram.nComponents);
        program.stats.put(OntologyAnalyzer.NO_SPECIAL_CONNECTED_COMPONENTS_D, newProgram.nSpecialComponents);
        return newProgram.sfccs.isEmpty();
    }

    private static boolean compatible(String s1, String s2) {
        if (s1.length() != s2.length())
            return false;
        Set<int[]> pairs = new HashSet<>();
        for (int i = 0; i < s1.length(); i++) {
            char a = s1.charAt(i);
            for (int j = i+1; j < s1.length(); j++) {
                char b = s1.charAt(j);
                if (a == b)
                    pairs.add(new int[]{i,j});
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
                char c = ann.charAt(j);
                ann.append(c);
            } else {
                ann.append(max);
                max++;
            }
            i++;
        }
        return ann.toString();
    }
}
