package db;

import primitives.*;

import java.sql.*;
import java.util.*;

public class Database {
    public Program program;
    public Map<String,Integer> recordCount = new HashMap<>();
    public Set<Predicate> extensional = new HashSet<>();
    public int size;
    public int limit = Integer.MAX_VALUE;
    public Connection conn;
    public Set<Fact> facts = new HashSet<>();
    public Set<Predicate> edbSchema = new HashSet<>();
    public String schemaName = "";

    public boolean isEmpty(Predicate predicate) {
        return !extensional.contains(predicate);
    }
}
