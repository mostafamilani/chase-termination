# Overview

This repository contains a tool for checking the termination of the chase procedure when applied for linear and simple linear existential. The tool is based on the algorithms in the paper [**"Semi-Oblivious Chase Termination for Linear Existential Rules"**](https://github.com/mostafamilani/chase-termination/blob/main/chase-termination.pdf). The repository also include a data generator and rule generator for testing the tool, and sample scenarios with data and rules that are generated using the data and rule generators.

The structure of the respository is as follows:
- \"scenarios\" includes the sample data and rules and their descrition. 
- \"generators\" contains the code for the data generator and the rule generator and a brief description of how they can be used.
- \"code\" includes the complete implementation of the termination algorithm for simple linear and linear ruels.

## Checking chase termination 

To run the chase termination algorithm for a set of rules "rules.txt", use the following command:

```java -jar chase-termination.jar -f rules.txt -d dbname -u username -p password -t n_tuples -o output.res```

where "-f" is a required option that specifies the input file that contains the set of rules. "-d dbname", "-U username", and "-P password" arespecify the database connection information for running the algorithm for linear rules. "-t n_tuples", e.g., "-t 1000" specifes the number of tuples in each table that are used in termination checking. If these database related inputs are included the tool will run the termination algorithm for linear rules, otherwise it runs the algorithm for simple linear rules. "-o output.res" is optional and specifies the output file name. The default output is "rules.res" if the input file is "res.txt". 

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
