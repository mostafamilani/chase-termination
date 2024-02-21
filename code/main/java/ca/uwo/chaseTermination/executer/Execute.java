package ca.uwo.chaseTermination.executer;

import ca.uwo.chaseTermination.synthesizer.DataGenerator;
import ca.uwo.chaseTermination.synthesizer.ProgramGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class Execute {
    private static final Logger log = LogManager.getLogger(Execute.class);

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            System.out.println("arg = " + arg);
        }
        if (AnalyzerExec.checkOption(args, "-dg"))
            executeClass(args, DataGenerator.class);
        else if (AnalyzerExec.checkOption(args, "-rg"))
            executeClass(args, ProgramGenerator.class);
        else if (AnalyzerExec.checkOption(args, "-f"))
            executeClass(args, OntologyAnalyzer.class);
        else printHelp();
    }

    public static void printHelp() {
        System.out.println("Usage of chase-termination.jar:\n");

        System.out.println("Data Generation:");
        System.out.println("java -jar chase-termination.jar -dg -d dbconfig -pr n_predicates -tp n_tuples -min min_arity -max max_arity -dm dm_size");
        System.out.println("    -dg: Specifies data generation mode.");
        System.out.println("    -d: The database configuration file.");
        System.out.println("    -pr: The number of predicates.");
        System.out.println("    -tp: The number of tuples.");
        System.out.println("    -min: The minimum arity of predicates.");
        System.out.println("    -max: The maximum arity of predicates.");
        System.out.println("    -dm: The data domain size.");

        System.out.println("Rule Generation:");
        System.out.println("java -jar chase-termination.jar -rg [-d dbconfig] -ru n_rules [-pr n_predicates -min min_arity -max max_arity] [-o rules.txt]");
        System.out.println("    -rg: Specifies rule generation mode.");
        System.out.println("    -ru: The number of rules.");
        System.out.println("    -o: (Optional) Output file and path (the default is rules.txt in the current directory).\n");

        System.out.println("Chase Termination:");
        System.out.println("java -jar chase-termination.jar -f input-file [-l -d dbconfig] [-o output.txt]");
        System.out.println("    -f: The path of the ontology input file for the chase termination process.");
        System.out.println("    -o: (Optional) Ouptut to the specified output file and its path (the default is input-file.res in the same directory of the input file).");
        System.out.println("    -l: (Optional) Checking for linear rules (the default is for simple linear rules).");
        System.out.println("    -d: The database configuration file (used for all operations).\n");
        System.out.println("    -ln: Removes non-linear rules before checking termination.\n");

        System.out.println("For any guidance or help, use -h to show this message.");
    }

    private static void executeClass(String[] args, Class<?> clazz) {
        int exit = 1;
        ArrayList<String> options = new ArrayList<>(Arrays.asList(args).subList(0, args.length));
        try {
            boolean rc = AnalyzerExec.exec(clazz, options);
            if (!rc)
                exit = 0;
        } catch (Exception e) {
            log.error("Errors occurred while running OntologyAnalyzer");
            e.printStackTrace();
        }
        System.exit(exit);
    }
}
