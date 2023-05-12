# Overview

This repository contains a tool for checking the termination of the chase procedure when applied for linear and simple-linear existential rules. The tool is based on the algorithms in the paper **"Semi-Oblivious Chase Termination for Linear Existential Rules: An Experimental Study"**.[^1] The repository also includes a data generator and rule generator for testing the tool, and sample scenarios with data and rules that are generated using the data and rule generators.

The structure of the respository is as follows:
- \"scenarios\" includes sample data and rules and their descrition. 
- \"generators\" contains the data generator and the rule generator and a brief description of how they can be used.
- \"code\" includes the complete implementation of the termination algorithm for simple-linear and linear rules.

## Checking Chase Termination 

To run the chase termination algorithm for a set of rules "rules.txt", use the following command:

```
java -jar chase-termination.jar -l -f rules.txt -d dbname -u username -p password
```

The option "-l" specifies whether the tool should run the termination algorithm for linear rules; if this option is missing, the tool will run the algorithm for simple-linear rules. The option "-f" is required and specifies the file containing the set of rules. 

When "-l" is present, the tool requires the database connection information specified by "-d dbname", "-u username", and "-p password". However, when "-l" is not given for simple-linear rules, these options are optional. If the database is missing, the algorithm for simple-linear rules assumes that all predicates have extensional data.

The option "-t n_tuples" is related to the algorithm for linear rules and specifies the number of tuples in each relation in the termination algorithm. If this option is missing, the algorithm uses all tuples in the relations.

To specify an output file name, use the option "-o output.res". The default output file name is "rules.res" if the input file is "rules.txt".

The tool executes the chase termination algorithm and returns whether the chase terminates. Additionally, the output file contains statistics about the program.

- terminates: true if the chase terminates; false otherwise.
- avg_arity, max_arity, min_arity: The average, maximum, and minimum arities of the predicates.
- n_rules, n_exist_vars, n_predicates: The numbers of rules, predicates, and the existential variables in the rules.
- n_nodes, n_edges, n_components, n_spacial_components, n_special_edges: The numbers of nodes, edges, fully connected components, and fully connected components with special edges in the dependency graph of the set of rules.
- t_parse: The time to parse the rules in the input file.
- t_graph: The time to build the dependency graph.
- t_comp: The time to find special fully connected components and check for their support.
- t_terminate: The end-to-end time to check termination.

For the algorithm for linear rules, which involves dynamic simplification, the tool returns additional statistics related to dynamic simplificaiton:
- n_nodes_d, n_edges_d, n_special_edges_d, n_components_d, n_spacial_components_d: The numbers of nodes, edges, special edges, fully connected components, and special fully connected components in the dependency graph of the dynamically simplified rules. 
- n_facts, n_shapes: The numbers of facts and shapes in the database.
- t_graph_d: The time required to build the dependency graph of the dynamically simplified rules. 
- t_shapes_d, t_shapes_m: The time to find the shapes (in-db and in-memory)


## Chase-based Checking 

We use VLog,[^2] a state-of-the-art reasoner, to implement an alternative chase-based algorithm for checking chase terminsion and compare its performance with our termination algorithms, for simpl-linear and linear rules. We extended VLog reasoner for running Skolem chase with an additional checking that monitors any recursive invocation of Skolem functions during the materalization of the chase instance to decide non-termination for linear tgds. You can run this chase-based termination checking algorithm using the following command:

```
java -jar chase-termination.jar -f rules.txt -d dbname -u username -p password -v
```
Note that the chase-based algorithm requires an extensional database that is specified in the command. We use the scenario with OWL ontologies to compare our termination algorithms with this baseline. The results show our algorithm for chase termination runs ~37 times faster that the chase-based baseline algorithm for ~72% of the OWL ontologies that both algorithms could successfully decide chase termination. For ~28% of the ontologies, the chase-base algorithm fails to decide due to running out of memory. 

[^1]: Calautti, Marco, Mostafa Milani, and Andreas Pieris. "Semi-Oblivious Chase Termination for Linear Existential Rules: An Experimental Study." arXiv preprint arXiv:2303.12851 (2023).
[^2]: https://github.com/karmaresearch/vlog
