package ca.uwo.chaseTermination.synthesizer;

import ca.uwo.chaseTermination.engine.PersistantDatabase;
import ca.uwo.chaseTermination.executer.AnalyzerExec;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class DataGenerator {
    public static void main(String[] args) throws IOException, SQLException {
        String dbCobfig = AnalyzerExec.getOptionValueWithAssert(args, "-db", 1);
        int npred = Integer.parseInt(AnalyzerExec.getOptionValueWithAssert(args, "-pr", 1));
        int ntuples = Integer.parseInt(AnalyzerExec.getOptionValueWithAssert(args, "-t", 1));
        int[] arity = new int[2];
        arity[0] = Integer.parseInt(AnalyzerExec.getOptionValueWithAssert(args, "-min", 1));
        arity[1] = Integer.parseInt(AnalyzerExec.getOptionValueWithAssert(args, "-max", 1));
        int dmsize = Integer.parseInt(AnalyzerExec.getOptionValueWithAssert(args, "-dm", 1));

        Properties prop = new Properties();
        prop.load(new FileInputStream(dbCobfig));

        PersistantDatabase database = new PersistantDatabase(prop);
        database.loadSchema();
        database.computeRecordCount();
        database.createDatabaseSchema(arity, npred);
        database.fillDatabase(dmsize, ntuples);
    }
}
