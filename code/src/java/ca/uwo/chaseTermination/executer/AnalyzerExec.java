package ca.uwo.chaseTermination.executer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AnalyzerExec {
    private static final Logger log = LogManager.getLogger(AnalyzerExec.class);

    public static void main(String[] args) throws IOException {
        integrateResultsIfNeeded(args);

        String inputDirPath = getInputDirectoryPath(args);

        List<File> files = getListOfFilesInDirectoryPath(inputDirPath);

        runOntologyAnalyzerModule(args, files);
    }

    private static void runOntologyAnalyzerModule(String[] args, List<File> files) {
        for (File next : files) {
            try {
                ArrayList<String> options = new ArrayList<>();
                if (checkOption(args, "-r"))
                    options.add("-r");
                options.add("-f");
                options.add(next.getAbsolutePath());
                options.addAll(Arrays.asList(args).subList(1, args.length));
                exec(OntologyAnalyzer.class, options);
            } catch (Exception e) {
                log.error(next.getName() + " return with error!\n\n");
                e.printStackTrace();
            }
        }
    }

    private static List<File> getListOfFilesInDirectoryPath(String inputDirPath) {
        List<File> files;
        File fileDirectory = new File(inputDirPath);
        if (fileDirectory.isDirectory())
            files = getFiles(inputDirPath);
        else
            files = new ArrayList<>(List.of(fileDirectory));
        return files;
    }

    private static String getInputDirectoryPath(String[] args) {
        String inputDirPath = getOptionValue(args, "-f", 1);
        if (inputDirPath == null || inputDirPath.isEmpty()) {
            log.error("No input directory specified!\n\n");
            System.exit(1);
        }
        return inputDirPath;
    }

    private static void integrateResultsIfNeeded(String[] args) throws IOException {
        String inputDirPath;
        if (checkOption(args, "-i")) {
            inputDirPath = getOptionValue(args, "-i", 1);
            if (inputDirPath == null || inputDirPath.isEmpty()) {
                log.error("No input directory specified!\n\n");
                System.exit(1);
            }
            integrateResults(inputDirPath);
            System.exit(0);
        }
    }

    private static List<File> getFilesInSubdir(String path) {
        File rootDir = new File(path);
        List<File> files = new ArrayList<>(List.of(rootDir.listFiles((dir, name) -> name.endsWith(".res"))));
        File[] subdirs = rootDir.listFiles(File::isDirectory);
        assert subdirs != null;
        for (File subdir : subdirs)
            files.addAll(List.of(subdir.listFiles((dir, name) -> name.endsWith(".res"))));
        return files;
    }

    private static void integrateResults(String path) throws IOException {
        List<File> files = getFilesInSubdir(path);

        Set<Map<String, Object>> results = new HashSet<>();
        List<String> keys = new ArrayList<>();
        keys.add("name");
        keys.add("t_vlog");
        for (File file : files) {
            log.info("Processing " + file.getName());
            Map<String, Object> result = new HashMap<>();
            BufferedReader in = new BufferedReader(new FileReader(file));
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            String ontologyName = file.getAbsolutePath().substring(path.length());
            result.put("name", ontologyName);
            String line = in.readLine().replaceAll(" ", "");
            List<Double> timesD = new ArrayList<>();
            while (line != null) {
                String key = line.substring(0, line.indexOf(":"));
                String value = line.substring(line.indexOf(":") + 1);
                if (key.equals(OntologyAnalyzer.TIME_TERMINATES_GRAPH))
                    timesD.add(Double.parseDouble(value));
                else {
                    result.put(key, value);
                    if (!keys.contains(key))
                        keys.add(key);
                }
                line = in.readLine();
            }
            double avg_t = average(timesD), std_t = std(timesD);
            String key = OntologyAnalyzer.TIME_TERMINATES_GRAPH + "_avg";
            result.put(key, avg_t);
            if (!keys.contains(key)) keys.add(key);
            key = OntologyAnalyzer.TIME_TERMINATES_GRAPH + "_std";
            result.put(key, std_t);
            if (!keys.contains(key)) keys.add(key);
            results.add(result);
        }
        String header = "";
        keys.sort((o1, o2) -> {
            if (o1.equals(o2)) return 0;
            if (o1.equals("name")) return -1;
            if (o2.equals("name")) return 1;
            return o1.compareTo(o2);
        });
        for (String key : keys) {
            header += key + ",";
        }
        String fileName = path + "/results.csv";
        File file = new File(fileName);
        file.createNewFile();
        FileWriter out = new FileWriter(file);
        out.write(header.substring(0, header.length() - 1) + "\n");

        for (Object o : results) {
            Map<String, Object> result = (Map<String, Object>) o;
            String line = "";
            for (String key : keys) {
                System.out.println("key = " + key);
                String value = result.get(key) + "";
                line += value + ",";
            }
            out.write(line.substring(0, line.length() - 1) + "\n");
        }
        out.close();
    }

    private static double std(List<Double> values) {
        double mean = average(values);
        double temp = 0;

        for (int i = 0; i < values.size(); i++) {
            double val = values.get(i);

            // Step 2:
            double squrDiffToMean = Math.pow(val - mean, 2);

            // Step 3:
            temp += squrDiffToMean;
        }

        // Step 4:
        double meanOfDiffs = (double) temp / (double) (values.size());

        // Step 5:
        return Math.sqrt(meanOfDiffs);
    }

    private static double average(List<Double> values) {
        double sum = 0;
        for (Double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    public static boolean checkOption(String[] args, String option) {
        for (String arg : args) {
            if (arg.equals(option)) return true;
        }
        return false;
    }

    public static boolean exec(Class klass, List<String> options) throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
        int mbRam = (int) (memorySize / 1024 / 1024);
        int vbRam = (int) (mbRam * 0.85);
        String className = klass.getCanonicalName();
        String vmRamParams = "-Xmx" + vbRam + "m";
        String stacksize = "-Xss2m";
        List<String> commands = new ArrayList<String>();
        commands.add(javaBin);
        commands.add("-cp");
        commands.add(classpath);
        commands.add(vmRamParams);
        commands.add(stacksize);
        commands.add(className);
        commands.addAll(options);
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = builder.start();
        process.waitFor(5, TimeUnit.MINUTES);
        int exitValue = 0;
        try {
            exitValue = process.exitValue();
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
            System.out.println("Process terminates!");
            process.destroy();
        }
        return (exitValue != 0); //Return true if errors
    }

    public static String getOptionValue(String[] args, String option, int valueIndex) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals(option))
                return args[i + valueIndex];
        }
        return null;
    }

    public static String getOptionValueWithAssert(String[] args, String option, int valueIndex) {
        String optionValue = getOptionValue(args, option, valueIndex);
        if (optionValue == null)
            throw new RuntimeException("Missing option: " + option);
        return optionValue;
    }

    public static List<File> getFiles(String inputDirPath) {
        List<File> dirs = new ArrayList<>();
        File dir = new File(inputDirPath);
        dirs.add(dir);

        File[] subdirs = dir.listFiles(File::isDirectory);
        assert subdirs != null;
        Collections.addAll(dirs, subdirs);

        List<File> files = new ArrayList<>();
        for (File o : dirs) {
            File[] tempFiles = o.listFiles(file -> {
                String suffix = ".txt";
                return file.getName().endsWith(suffix);
            });
            assert tempFiles != null;
            System.out.println("tempFiles.length = " + tempFiles.length);
            Collections.addAll(files, tempFiles);
        }
        return files;
    }
}
