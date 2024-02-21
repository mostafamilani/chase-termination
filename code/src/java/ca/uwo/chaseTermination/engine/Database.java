package ca.uwo.chaseTermination.engine;

import ca.uwo.chaseTermination.primitives.Fact;
import ca.uwo.chaseTermination.primitives.Predicate;
import ca.uwo.chaseTermination.primitives.Term;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public abstract class Database {
    public Program program;
    public Schema schema = new Schema();

    public Map<Predicate, Integer> recordCount = new HashMap<>();

    public static int getDBSize(Program program) {
        int size = 0;
        for (Integer count : program.database.recordCount.values())
            size += count;
        return size;
    }

    public abstract Map<Predicate, Set<String>> findShapes() throws SQLException;

    public abstract void printDBStats() throws SQLException, IOException;

    public abstract boolean isEmpty(Predicate predicate);

    public boolean isEmpty() {
        for (Predicate predicate : recordCount.keySet()) {
            if (recordCount.get(predicate) > 0)
                return false;
        }
        return true;
    }

    public abstract Set<Type> findTypes();
}
