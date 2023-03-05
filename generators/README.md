# Synthetic Data and Rule Generation

This repository contains generators for synthetic data and rules. The generators receive tuning parameters that characterize the database and the set of rules to be generated. These generators consider possible shape; The data generator selects tuples of different shapes and the rule generator uses atoms of different shapes in the body of the rules. The applications of these generators are briefly explained below.

## Data Generator

To generate a synthetic database, run the following command:

```
java -jar data-generator.jar -u username -p password -d dbname -t n_tuples -min min_arity -max max_arity -dm dm_dize
```

Here, "username" and "password" are the credentials for the database connection. "dbname" is the name of the database instance to be filled with new tuples (it must be created before running the database generator). "n_tuples" specifies the number of tuples in each relation. "min_arity" and "max_arity" specify the minimum and the maximum number of attributes in each relation, respectively. "dm_size" specifies the size of the domain set, i.e., the range of values that can be assigned to the attributes.

The generator returns a file "dbname.txt" that contains schema information of the generated database. This file can be passed to the rule-generator to use the same schema. The relations have schema "P_i(c_1,c_2,...,c_k)" where i is in [1,n_relations] and k is randomly selected from the range [min_arity,max_arity]. The tuples in the relations are from a domain set {1,2,...,dm_size}.

## Rule Generator

To generate synthetic rules, run the following command:

```
java -jar rule-generator.jar -l -r n_rules -p n_predicates -min min_arity -max max_arity -o rules.txt
```
Here, "-l" indicates that the rules should be linear, and if omitted, the rules will be simple linear. "n_rules" specifies the number of rules to be generated. "n_predicates" specifies the number of predicates in the body of each rule. "min_arity" and "max_arity" specify the minimum and the maximum number of arguments in each predicate, respectively. "rules.txt" is the file where the rules will be written.
