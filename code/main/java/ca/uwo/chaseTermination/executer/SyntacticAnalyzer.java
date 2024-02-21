package ca.uwo.chaseTermination.executer;

import ca.uwo.chaseTermination.engine.Database;
import ca.uwo.chaseTermination.engine.Program;
import ca.uwo.chaseTermination.graph.*;
import ca.uwo.chaseTermination.primitives.*;
import ca.uwo.chaseTermination.synthesizer.ProgramGenerator;

import java.util.*;

public class SyntacticAnalyzer {
    public static String[] annotations = generateAnnotations(10);

    public static int[] annotationCounts = {1, 2, 5, 15, 52, 203, 877, 4140, 21147, 115975, 678570, 4213597, 27644437};

    public static Set<Position> getInfiniteRankPositions(Map<Position, Node> dGraph, Set<Node> nodesInSpecialCycle) {
        return getPositions(findDescendants(nodesInSpecialCycle, false));
    }

    public static Set<Node> findDescendants(Set<Node> nodes, boolean reverse) {
        Set<Node> ancestors = new HashSet<Node>();
        Set<Node> newAncestors = new HashSet<Node>();

        newAncestors.addAll(nodes);
        Set<Node> visited = new HashSet<>();
        while (!newAncestors.isEmpty()) {
            Set<Node> temp = new HashSet<Node>();
            for (Node node : newAncestors) {
                if (visited.contains(node)) continue;
                ancestors.add(node);
                temp.addAll(node.nextNodes());
                visited.add(node);
            }
            temp.removeAll(ancestors);
            newAncestors = temp;
        }
        return ancestors;
    }

    public static Set<FCComponent> findFullyConnectedComponents(Map<Position, Node> dependencyGraph) {
        Collection<Node> nodes = dependencyGraph.values();
        Set<FCComponent> components = new HashSet<>();
        for (Node node : nodes) {
            if (node.p == null)
                System.out.println("node = " + node);
            if (node.index == -1)
                components.addAll(strongConnectNonR(node));
        }
        return components;
    }

    public static Set<Node> getNodesInSpecialCycle(Set<FCComponent> components, Program program) {
        HashSet<Node> result = new HashSet<>();
        for (FCComponent component : components) {
            if (component.special && !component.members.isEmpty()) {
                result.addAll(component.members);
                program.increamentStat(OntologyAnalyzer.NO_SPECIAL_CONNECTED_COMPONENTS);
            }
        }
        return result;
    }

    public static Set<Position> getPositions(Set<Node> nodes) {
        HashSet<Position> positions = new HashSet<>();
        for (Node node : nodes)
            positions.add(node.p);
        return positions;
    }

    public static Set<FCComponent> strongConnectNonR(Node node) {
        Set<FCComponent> components = new HashSet<>();

        int preCount = 0;
        Stack stack = new Stack();
        Stack minStack = new Stack();
        Stack iterStack = new Stack();
        Set initi = new HashSet();
        Edge e = new Edge();
        initi.add(e);
        e.destination = node;
        Iterator<Edge> iterator = initi.iterator();
        while (true) {
            if (iterator.hasNext()) {
                Edge edge = iterator.next();
                Node v = edge.destination;
                if (v.index == -1) {
                    v.lowLink = preCount;
                    v.index = preCount;
                    preCount++;
                    stack.push(v);
                    minStack.push(v.lowLink);
                    iterStack.push(v);
                    iterStack.push(iterator);
                    iterator = v.nextEdges.iterator();
                } else if (minStack.size() > 0) {
                    int min = (int) minStack.pop();
                    if (v.lowLink < min) min = v.lowLink;
                    minStack.push(min);
                }
            } else {
                if (iterStack.size() == 0) break;

                iterator = (Iterator<Edge>) iterStack.pop();
                Node v = (Node) iterStack.pop();
                int min = (int) minStack.pop();

                if (min < v.lowLink) {
                    v.lowLink = min;
                } else {
                    FCComponent component = new FCComponent();
                    Node wNode;
                    do {
                        wNode = (Node) stack.pop();
                        component.members.add(wNode);
                        wNode.lowLink = Integer.MAX_VALUE;
                    } while (!wNode.equals(v));
                    components.add(component);
                }
                if (minStack.size() > 0) {
                    min = (int) minStack.pop();
                    if (v.lowLink < min) min = v.lowLink;
                    minStack.push(min);
                }
            }
        }
        for (FCComponent component : components) {
            Set<Node> nodes = component.members;
            for (Node n : nodes) {
                for (Edge edge : n.nextEdges) {
                    if (nodes.contains(edge.destination) && edge.special) {
                        component.special = true;
                        break;
                    }
                }
                if (component.special)
                    break;
            }
        }
        return components;
    }

    public static Map<Position, Node> buildDependencyGraph(Set<TGD> rules) {
        HashMap<Position, Node> graph = new HashMap<>();
        for (TGD rule : rules)
            updateGraph(graph, rule);
        return graph;
    }

    public static void updateGraph(Map<Position, Node> graph, TGD rule) {
        Collection<Variable> variables = rule.variables.values();
        Set<Variable> existentialVariables = rule.existentialVars;
        Conjunct body = rule.body;
        Conjunct head = rule.head;
        for (Variable variable : variables) {
            if (!variable.isBody() || !rule.isFrontier(variable))
                continue;
            Set<Node> bodyNodes = fetchNode(graph, getPositionsInConjunct(variable, body));
            Set<Node> headNodes = fetchNode(graph, getPositionsInConjunct(variable, head));

            for (Node b : bodyNodes) {
                for (Variable evar : existentialVariables) {
                    Set<Node> nextSpecials = fetchNode(graph, getPositionsInConjunct(evar, head));
                    for (Node next : nextSpecials)
                        b.addNext(next, true);
                }
                for (Node next : headNodes)
                    b.addNext(next, false);
            }
        }
    }

    public static Set<Node> fetchNode(Map<Position, Node> graph, Set<Position> positions) {
        HashSet<Node> nodes = new HashSet<>();
        for (Position position : positions) {
            if (!graph.containsKey(position))
                graph.put(position, new Node(position));
            nodes.add(graph.get(position));
        }
        return nodes;
    }

    public static Set<Position> getPositionsInConjunct(Variable variable, Conjunct conjunct) {
        HashSet<Position> positions = new HashSet<>();
        List<PositiveAtom> atoms = conjunct.getAtoms();

        for (Atom atom : atoms) {
            int pos = 0;
            List<Term> terms = atom.terms;
            for (Term term : terms) {
                if (term.label.equals(variable.label))
                    positions.add(new Position(pos, atom.predicate));
                pos++;
            }
        }
        return positions;
    }

    public static boolean containsAll(Set<Position> ps1, Set<Position> ps2) {
        for (Position position : ps2) {
            if (!ps1.contains(position)) return false;
        }
        return true;
    }

    public static boolean isRepeated(Variable variable, TGD tgd) {
        boolean appeared = false;
        for (Atom atom : tgd.body.getAtoms()) {
            for (Term term : atom.terms) {
                if (variable.equals(term)) {
                    if (appeared)
                        return true;
                    else
                        appeared = true;
                }
            }
        }
        return false;
    }

    public static List<FCCNode> addEdgesToNodes(List<FCCNode> nodes) {
        for (FCCNode fccNode1 : nodes) {
            for (FCCNode fccNode2 : nodes) {
                if (fccNode1 == fccNode2)
                    continue;
                for (Node node1 : fccNode1.component.members) {
                    Edge edge = determineEdgeForFCCNodes(node1, fccNode2);
                    if (edge != null)
                        fccNode1.nextEdges.add(edge);
                }
            }
        }
        return nodes;
    }

    public static List<FCCNode> createFCCNodes(Set<FCComponent> fcComponents) {
        List<FCCNode> nodes = new ArrayList<>();
        for (FCComponent component : fcComponents) {
            FCCNode node = new FCCNode();
            node.component = component;
            node.special = component.special;
            nodes.add(node);
        }
        return nodes;
    }

    public static List<FCCNode> createFCCGraph(Set<FCComponent> fcComponents, Database database) {
        List<FCCNode> nodes = createFCCNodes(fcComponents);
        nodes = addEdgesToNodes(nodes);
        nodes = markNodes(nodes, database);
        return nodes;
    }

    public static List<FCCNode> markNodes(List<FCCNode> nodes, Database database) {
        for (FCCNode node : nodes) {
            for (Node n : node.component.members) {
                if (!database.isEmpty(n.p.predicate))
                    node.marked = true;
            }
        }
        return nodes;
    }

    public static List<FCCNode> findAllMarkedNodes(List<FCCNode> nodes) {
        List<FCCNode> markedNodes = new ArrayList<>();
        for (FCCNode node : nodes)
            if (node.marked)
                markedNodes.add(node);
        return markedNodes;
    }

    public static void depthFirstSearch(List<FCCNode> nodes) {
        List<FCCNode> markedNodes = findAllMarkedNodes(nodes);
        for (FCCNode node : markedNodes) {
            if (node.visited)
                continue;
            DFS(node);
        }
    }

    public static void DFS(FCCNode node) {
        Stack<FCCNode> stack = new Stack<FCCNode>();
        stack.push(node);
        while (!stack.isEmpty()) {
            FCCNode current = stack.pop();
            current.visited = true;
            current.marked = true;
            for (Edge edge : current.nextEdges)
                if (!((FCCNode) edge.destination).visited)
                    stack.push((FCCNode) edge.destination);
        }
    }

    public static List<FCCNode> createComponentGraph(Set<FCComponent> fcComponents, Database database) {
        List<FCCNode> nodes = createFCCGraph(fcComponents, database);
        return nodes;
    }

    public static Edge determineEdgeForFCCNodes(Node node1, FCCNode fccNode2) {
        Edge newEdge = new Edge();
        for (Edge edge : node1.nextEdges) {
            if (fccNode2.component.members.contains(edge.destination)) {
                newEdge.destination = fccNode2;
                if (edge.special) {
                    newEdge.special = true;
                    return newEdge;
                }
            }
        }
        if (newEdge.destination == null)
            return null;
        return newEdge;
    }

    public static Program simplify(Program program) {
        long startTime = System.nanoTime();
        Program result = new Program();
        int c = 0;
        for (TGD tgd : program.tgds) {
            c += simplify(tgd, program);
        }
        System.out.println("#Total number of rules in the initial program: " + program.tgds.size());
        System.out.println("#Total number of rules in the simplified program: " + c);
        long endTime = System.nanoTime();
        System.out.println("#time (sec) " + ((endTime - startTime) / 1000000000F));
        return result;
    }

    public static int simplify(TGD tgd, Program program) {
        int c = 0;
        System.out.println("#Simplifying rule: " + tgd);
        if (tgd.body.getAtoms().size() != 1) throw new RuntimeException("Simplifying a non linear program!");
        List<Variable> vs = tgd.body.getAtoms().get(0).getVariables();
        for (int i = 0; i < annotationCounts[vs.size() - 1]; i++) {
            String ant = annotations[i].substring(0, vs.size());
            simplify(program, tgd, ant);
            c++;
        }
        return c;
    }

    public static TGD simplify(Program program, TGD tgd, String ant) {
        Atom b = tgd.body.getAtoms().get(0);
        Atom h = tgd.head.getAtoms().get(0);
        List<Variable> vs = b.getVariables();
        List<Term> hTerms = new ArrayList<>(h.terms);
        String hAnnot = "";
        String completeAn = "";
        List<Term> newTerms = new ArrayList<>();
        List<Variable> removeDup = new ArrayList<>();
        for (Variable v : vs) {
            if (!removeDup.contains(v))
                removeDup.add(v);
        }
        vs = removeDup;
        for (Term var : b.terms) {
            int index = b.terms.indexOf(var);
            String s = "" + ant.charAt(index);
            Variable nVar = vs.get(Integer.parseInt(s, 16) - 1);
            newTerms.add(nVar);
            completeAn += s;

            if (nVar != var && hTerms.contains(var)) {
                hTerms.replaceAll(term -> {
                    if (term == var)
                        return nVar;
                    return term;
                });
            }
        }
        List<Variable> hvs = new ArrayList<>();
        for (Term term : hTerms) {
            if (!hvs.contains(term))
                hvs.add((Variable) term);
            hAnnot += "" + (hvs.indexOf(term) + 1);
        }
        List<Term> temp = new ArrayList<>();
        for (Term term : hTerms) {
            if (!temp.contains(term)) temp.add(term);
        }
        hTerms = temp;
        temp = new ArrayList<>();
        for (Term term : newTerms) {
            if (!temp.contains(term)) temp.add(term);
        }
        newTerms = temp;
        PositiveAtom nB = new PositiveAtom(program.database.schema.fetchPredicate(b.predicate.name + "@" + completeAn, b.predicate.arity), newTerms);
        PositiveAtom nH = new PositiveAtom(program.database.schema.fetchPredicate(h.predicate.name + "@" + hAnnot, h.predicate.arity), hTerms);
        TGD result = new TGD();
        result.body = new Conjunct();
        result.body.add(nB);
        result.head = new Conjunct();
        result.head.add(nH);
        result.initVars();
        ProgramGenerator.setExistentialVariables(result);
        program.tgds.add(result);
        return result;
    }

    private static String[] generateAnnotations(int count) {
        Set<String> annotations = new HashSet<>();
        annotations.add("1");
        for (int i = 2; i <= count; i++) {
            Set<String> newAns = new HashSet<>();
            for (int j = i - 1; j >= 1; j--) {
                for (String annotation : annotations) {
                    int max = 0;
                    for (int k = 0; k < annotation.length(); k++) {
                        int n = Integer.parseInt(annotation.charAt(k) + "");
                        if (n > max) max = n;
                    }
                    max++;
                    String newAn1 = annotation + Integer.toHexString(max);
                    newAns.add(newAn1);
                    String newAn2 = annotation + Integer.toHexString(getValueIndex(annotation, j));
                    newAns.add(newAn2);
                }
            }
            annotations = newAns;
        }
        String res[] = annotations.toArray(new String[annotations.size()]);
        Arrays.sort(res, Comparator.comparing(o -> new StringBuilder(o).reverse().toString()));
        return res;
    }

    private static int getValueIndex(String annotation, int j) {
        int c = Integer.parseInt(annotation.charAt(j - 1) + "", 16);
        for (int i = 0; i < j - 2; i++) {
            int ch = Integer.parseInt(annotation.charAt(i) + "");
            if (ch == c)
                return getValueIndex(annotation, c);
        }
        return c;
    }

    /*public static void main(String[] args) throws IOException {
        File dir = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\linear");
        File[] files = dir.listFiles();
        for (File file : files) {
            Program program = Parser.parseProgram(file, database);
            String outName = file.getAbsolutePath().replace(".txt", "-sim.txt").replace("linear", "simplified");
            File out = new File(outName);
            out.createNewFile();
            System.setOut(new PrintStream(out));
            simplify(program);
        }
    }*/
}

