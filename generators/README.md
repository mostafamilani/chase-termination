# Synthetic Data and Rule Generation

The generators are recieved tunning parameters that characterize the database and the set of rules to be generated. The data generator generates a database in which tuples with different shapes appear. The rule generator generates rules with body atoms of different shapes. The applications of these generators is briefly explained next.

## Data Generator

The following command invokes the data generator and creates a database with name "dbname" with "n_relations" relations each of which has "n_tuples" tuples in it. 

```
dbgenerate -U username -P password -d dbname -size n_tuples -min min_attr -max max_attr -dm dm_dize
```

"min_att" and "max_att" specify the minimum and the maximum number of attributes in each relation. The generator returns a file "dbname.txt" that contains schema information of the generated database. The relations have schema "P_i(c_1,c_2,...,c_k)" where i is in [1,n_relations] and k is randomly selected from the range [min_att,max_att]. The tuples in the relations are from a domain set {1,2,...,dmsize}.

## Rule Generator

To invoke the rule generator, run the following command:

```
rgenerate -s/-l -r n_rules -p n_predicates -min min_arity -max max_arity -o rules.txt
```
The resuls is a set of simple linear or linear depending on whether "-s" or "-l" are respectively included in the command. "n_rules", "n_predicates", "min_arity", and "max_arity" specify the characterisitcs of the set of rules, and "rules.txt" is the output file where the rules are returned.

<!--
createdb -U username -O ownername -E UTF8 -T template0 -l en_US.UTF-8 databasename
psql -U username -d databasename -f filename.sql
pg_restore -U postgres -C -d chasedb d.sql
-->

