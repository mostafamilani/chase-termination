package exe;

import exceptions.InvalidOptionException;

public class AnalyzerExec {
    public static boolean checkOption(String[] args, String option) {
        for (String arg : args) {
            if (arg.equals(option)) return true;
        }
        return false;
    }

    public static String getOptionValue(String[] args, String option) {
        int i = 0;
        for (String arg : args) {
            if (arg.equals(option)) return args[i+1];
            i++;
        }
        return null;
    }

    public static String getOptionValue(String[] args, String option, boolean notNull) throws InvalidOptionException {
        int i = 0;
        for (String arg : args) {
            if (arg.equals(option)) return args[i+1];
            i++;
        }
        if (notNull)
            throw new InvalidOptionException(option);
        return null;
    }
}
