package exe;

import db.Program;
import primitives.TGD;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DLGPGenerator {
    public static void printProgram(File outFile, Program program) throws IOException {
        FileWriter out = new FileWriter(outFile);
        for (TGD tgd : program.tgds) {
            out.write(tgd.toString().replaceAll(":-", " :- ") + ".\n");
        }
        out.close();
    }
}
