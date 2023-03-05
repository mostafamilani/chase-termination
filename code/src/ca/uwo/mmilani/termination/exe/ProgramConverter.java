package ca.uwo.mmilani.termination.exe;

import java.io.*;
import java.util.*;

public class ProgramConverter {
    public static void main(String[] args) throws IOException {
        Map<String, Set<String>> files = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            String deep = "/home/cqadev/Desktop/chase-termination/real-data/chase-termination/deep_" + i + "00.txt";
            files.put(deep, new HashSet<>());
            files.get(deep).add("/home/cqadev/Desktop/chase-termination/real-data/deep/" + i + "00/dependencies/deep.t-tgds.txt");
            files.get(deep).add("/home/cqadev/Desktop/chase-termination/real-data/deep/" + i + "00/dependencies/deep.st-tgds.txt");
        }

        String[] datasets = new String[] {"Ontology-256", "LUBM", "STB-128"};
        for (int i = 0; i < datasets.length; i++) {
            String dataset = datasets[i];
            String ds = "/home/cqadev/Desktop/chase-termination/real-data/chase-termination/" + dataset + ".txt";
            files.put(ds, new HashSet<>());
            files.get(ds).add("/home/cqadev/Desktop/chase-termination/real-data/" + dataset + "/dependencies/" + dataset + ".t-tgds.txt");
            files.get(ds).add("/home/cqadev/Desktop/chase-termination/real-data/" + dataset + "/dependencies/" + dataset + ".st-tgds.txt");
        }

        for (String outpath : files.keySet()) {
            File outfile = new File(outpath);
            outfile.createNewFile();
            FileWriter out = new FileWriter(outfile);
            for (String inpath : files.get(outpath)) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(inpath));
                    while(true) {
                        String line = in.readLine();
                        if (line == null) break;
                        line = line.replace(" ", "");
                        System.out.println("line = " + line);
                        Set<String> lines = covertLine(line);
                        for (String outLine : lines) {
                            out.write(outLine + ".\n");
                            System.out.println("outLine = " + outLine);
                        }
                    }
                    System.out.println(inpath + " processed! \n");
                } catch (Throwable e) {
                    System.out.println("outfile = " + outfile);
                    e.printStackTrace();
                    System.out.println("Cannot parse " + inpath + "! \n");
                    System.exit(1);
                }
            }
            out.close();
        }
    }

    private static Set<String> covertLine(String line) {
//    "m87004(?X1,?X2,?X7,?X8) -> m298004(?X2,?X3,?X9,?X10), m113004(?X3,?X4,?X11,?X12), m299004(?X0,?X1,?X5,?X6)."
//    "p_316(X2,X3,X1,X1,X2,X2,X1,X15,X2) :- p_710(X1,X2,X1,X3,X2,X2,X3)."
        HashSet<String> lines = new HashSet<>();
        line = replaceVars(line);
        String body = line.substring(0, line.indexOf("->"));
        if (body.contains("),")) {
            System.out.println("Nonlinear rule: " + line);
            return lines;
        }
        String head = line.substring(line.indexOf("->") + 2);
        while(head.contains("),")) {
            lines.add(head.substring(0, head.indexOf("),") + 1) + ":-" + body);
            int begin = head.indexOf("),");
            head = head.substring(begin + 2);
        }
        lines.add(head.substring(0, head.length()-1) + ":-" + body);
        return lines;
    }

    private static String replaceVars(String line) {
        List<String> vars = new ArrayList<>();
        vars.addAll(findVars(line));
        String newline = line;
        int i = 1;
        Collections.sort(vars, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return t1.compareTo(s);
            }
        });
        for (String var : vars) {
            newline = newline.replace("?" + var, "X" + i);
            i++;
        }
        return newline;
    }

    private static Set<String> findVars(String line) {
        HashSet<String> vars = new HashSet<>();
        while(line.contains("?")) {
            int end = line.indexOf(",");
            if (end == -1 || line.indexOf(")") < end)
                end = line.indexOf(")");
            if (end != 0)
                vars.add(line.substring(line.indexOf("?")+1, end));
            line = line.substring(end+1);
        }
        return vars;
    }
}
