# Overview

This repository contains a tool for checking the termination of the chase procedure when applied for linear and simple-linear existential rules. The tool is based on the algorithms in the paper **"Semi-Oblivious Chase Termination for Linear Existential Rules: An Experimental Study"**.[^1] The repository also includes a data generator and rule generator for testing the tool, and sample scenarios with data and rules that are generated using the data and rule generators.

The structure of the respository is as follows:
- \"scenarios\" includes sample data and rules and their description. 
- \"generators\" contains the data generator and the rule generator and a brief description of how they can be used.
- \"code\" includes the complete implementation of the termination algorithm for simple-linear and linear rules.

## Checking Chase Termination 

This work focuses on a type of algorithms based on acyclicity checking in the dependency graph of rules. To run the algorithms for a set of rules "rules.txt", use the following command:

```
java -jar chase-termination.jar -l -f rules.txt -d dbname -u username -p password
```

The option "-l" specifies whether the tool should run the termination algorithm for linear rules; if this option is missing, the tool will run the algorithm for simple-linear rules. The option "-f" is required and specifies the file containing the set of rules. 

When "-l" is present, the tool requires the database connection information specified by "-d dbname", "-u username", and "-p password". However, when "-l" is not given for simple-linear rules, these options are optional. If the database is missing, the algorithm for simple-linear rules assumes that for each relation, there is at least one tuple in the database for that relation.

The option "-t n_tuples" is related to the algorithm for linear rules and specifies the number of tuples in each relation in the termination algorithm. If this option is missing, the algorithm uses all tuples in the relations.

To specify an output file name, use the option "-o output.res". The default output file name is "rules.res" if the input file is "rules.txt".

The tool executes the chase termination algorithm and returns whether the chase terminates. Additionally, the output file contains statistics about the program.

- terminates: true if the chase terminates; false otherwise.
- avg_arity, max_arity, min_arity: The average, maximum, and minimum arities of the predicates.
- n_rules, n_predicates, n_exist_vars: The numbers of rules, predicates, and the existential variables in the rules.
- n_nodes, n_edges, n_components, n_special_components, n_special_edges: The numbers of nodes, edges, strongly connected components, and strongly connected components with special edges, and number of special edges in the dependency graph of the set of rules.
- t_parse: The time to parse the rules in the input file.
- t_graph: The time to build the dependency graph.
- t_comp: The time to find strongly connected components with a special edge and check for their support.
- t_terminate: The end-to-end time to check termination.

For the algorithm for linear rules, which involves dynamic simplification, the tool returns additional statistics related to dynamic simplification:
- n_nodes_d, n_edges_d, n_special_edges_d, n_components_d, n_special_components_d: The numbers of nodes, edges, special edges, strongly connected components, and strongly connected components with a special edge in the dependency graph of the dynamically simplified rules. 
- n_facts, n_shapes: The numbers of facts and shapes in the database.
- t_graph_d: The time required to build the dependency graph of the dynamically simplified rules. 
- t_shapes_d, t_shapes_m: The time to find the shapes (in-db and in-memory)


## Chase-based Checking 

We use VLog,[^2], a state-of-the-art reasoner, to implement an alternative materialization-based algorithm for checking chase termination, and compare its performance with our acyclicity-based algorithms, for simpl-linear and linear rules. The materialization-based algorithm constructs the chase instance using the VLog reasoner with input a database and set of rules. By exploiting known bounds on the maximum size that the chase instance w.r.t. linear rules can have when it is finite, our algorithm concludes non-termination if at any point, the instance being built exceeds the bound, otherwise termination is concluded.
To run the materialization-based algorithm, use the following command:

```
java -jar chase-termination.jar -f rules.txt -d dbname -u username -p password -v
```
Note that the materialization-based algorithm requires an extensional database that is specified in the command. We use the scenario with real-world OWL ontologies to compare our acyclicity-based algorithms with this baseline. The results show that for ~28% of the ontologies, the materialization-base algorithm fails to decide due to running out of memory. For the remaining ~72% of the ontologies, our acyclicity-based algorithm for chase termination runs consistently faster that the materialization-based baseline.

[^1]: Calautti, Marco, Mostafa Milani, and Andreas Pieris. "Semi-Oblivious Chase Termination for Linear Existential Rules: An Experimental Study." arXiv preprint arXiv:2303.12851 (2023).
[^2]: https://github.com/karmaresearch/vlog
