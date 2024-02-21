# Synthetic Data and Rule Generation

This repository is dedicated to the generation of synthetic data and rules. Here, we provide generators capable of producing databases and rules with varying structures. These generators are designed to assist in the evaluation of chase termination algorithms. Below, we introduce the applications of our generators.

## Data Generator

To generate a synthetic database using the Data Generator, execute the following command:

```
java -jar chase-termination.jar -dg -d dbconfig -pr n_predicates -tp n_tuples -min min_arity -max max_arity -dm dm_size
```


The following are the parameters of the data generator:
- `-dg`: Specifies data generation mode.
- `-d`: The database configuration file includes key-value pairs for username, password, dbname, and url.
- `-pr`: The number of predicates (relations) in the database.
- `-tp`: The number of tuples (records).
- `-min`: The minimum arity of predicates (relation columns).
- `-max`: The maximum arity of predicates.
- `-dm`: The data domain size.

The data generator makes a database using the given schema details and then fills it with random records based on the parameters. It sets up relations P_i(c_1,c_2,...,c_k) where i varies from 1 to the total number of predicates (relationships), and k is chosen randomly within the limits set by the minimum and the maximum number of columns. randomly picked from a set of values that start at 1 and go up to the chosen domain size.

## Rule Generator

To generate synthetic rules, run the following command:

```
java -jar chase-termination.jar -rg [-l] [-d dbconfig] -ru n_rules [-pr n_predicates -min min_arity -max max_arity] [-o rules.txt]
```

The following are the parameters of the rule generator:
- `-rg`: Specifies rule generation mode.
- `-l`: (Optional) Indicates linear rule generation. By default, the generator creates simple linear rules.  
- `-d`: (Optional) The database with an existing schema to be used as the schema for the rules.
- `-ru`: The number of rules to be generated.
- `-o`: (Optional) The output file for the rules. If not specified, the default is `rules.txt` in the current directory.

The rule generator operates in two modes. In the first mode, it uses the schema information from an existing database, accessed through `-d dbconfig`, where the connection details are specified in dbconfig. In the second mode, it randomly generates the schema, with the additional parameters `-pr`, `-min`, and `-max` determining the predicates in the rules. 
