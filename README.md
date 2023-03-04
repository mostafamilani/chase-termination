# Overview

This repository contains a tool for checking chase termination for linear and simple linear existential rules introduced in the paper [**"Semi-Oblivious Chase Termination for Linear Existential Rules"**](https://github.com/mostafamilani/chase-termination/blob/main/chase-termination.pdf) as well as a data generator and rule generator for testing the tool. The repository also includes sample data and rules that are generated using the data and rule generators.

The structure of the respository is as follows:
- \"scenarios\" includes the sample data and rules and their descrition. 
- \"generators\" contains the code for the data generator and the rule generator and a brief description of how they can be used.
- \"chase-termination\" includes the complete implementation of the termination algorithm for simple linear and linear ruels.

## Checking chase termination 

To run the chase termination algorithm for a set of rules "rules.txt", use the following command:

```check -f rules.txt -d dbname -dsize size -U username -P password -o output.res```

where "-f" is a required option that specifies the input file that contains the set of rules. "-d dbname", "-U username", and "-P password" arespecify the database connection information for running the algorithm for linear rules. "-dsize size", e.g., "-dbsize 1000" specifes the number of tuples in each table that are used in termination checking. If these database related inputs are included the tool will run the termination algorithm for linear rules, otherwise it runs the algorithm for simple linear rules. "-o output.res" is optional and specifies the output file name. The default output is "rules.res" if the input file is "res.txt". 

The tool runs the chase termination algorithm, and returns whether the chase terminates along with the following additional statistics about the program in the output file:

- terminates: true if the chase terminats; false otherwise.
- avg_arity, max_arity, min_arity: The average, maximum, and minimum arities of the predicates.
- n_rules, n_exist_vars, n_predicates: The numbers of rules, predicates, and the existential variables in the rules.
- n_nodes, n_edges, n_components, n_spacial_components, n_special_edges: The numbers of nodes, edges, fully connected components, and fully connected components with special edges in the dependency graph of the set of rules.
- t_parse: The time to parse the rules in the input file.
- t_graph: The time to build the dependency graph.
- t_comp: The time to find special fully connected components and check for their support.
- t_terminate: The end-to-end time to check termination.

For the algorithm for linear rules, which involves dynamic simplification, the tool returns additional statistics related to dynamic simplificaiton:
- n_nodes_d, n_edges_d, n_special_edges_d, n_components_d, n_spacial_components_d: The number of nodes, edges, special edges, fully connected components, and special fully connected components in the dependency graph of the dynamically simplified rules. 
- n_facts, n_shapes: The number of facts and shapes in the database.
- t_graph_d: The time required to build the dependency graph of the dynamically simplified rules. 
- t_shapes_d, t_shapes_m: The time to find the shapes (in-db and in-memory)

## Synthetic Data and Rule Generation

The generators are recieved tunning parameters that characterize the database and the set of rules to be generated. The data generator generates a database in which tuples with different shapes appear. The rule generator generates rules with body atoms of different shapes. The applications of these generators is briefly explained next.

### Data generator

The following command invokes the data generator and creates a database with name "dbname" with "n_relations" relations each of which has "n_tuples" tuples in it. 

```
dbgenerate -U username -P password -d dbname -size n_tuples -min min_attr -max max_attr -dm dm_dize
```

"min_att" and "max_att" specify the minimum and the maximum number of attributes in each relation. The generator returns a file "dbname.txt" that contains schema information of the generated database. The relations have schema "P_i(c_1,c_2,...,c_k)" where i is in [1,n_relations] and k is randomly selected from the range [min_att,max_att]. The tuples in the relations are from a domain set {1,2,...,dmsize}.

### Rule Generator

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



## Scenarios

In this reposiroty, there is data about two types of scenarios that are used in the experimental evaluation of the paper associated with this respository:

``` psql -U```

- Synthetic scenarios: These scenarios include 45 sets of linear rules and 900 sets of simple linear rules as detailed in the paper. These sets of rules can be downloaded using this link where a database dump can be found for testing the linear rules. The following command creates a database with name "chasedb" and imports the dump in the database.
- Scenarios from the existing sets of rules in the literature:  
