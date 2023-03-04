This repository contains a tool for checking chase termination for linear and simple linear existential rules, as well as a data generator and rule generator for testing the tool. The repository also includes sample data and rules that are generated using the data and rule generators.

The structure of the respository is as follows:
- scenarios includes the sample data and rules and their descrition. 
- generators contains the code for the data generator and the rule generator and a brief description of how they can be used.
- chase-termination includes the complete implementation of the termination algorithm for simple linear and linear ruels.

To run the chase termination algorithm for a simple linear program "program.txt", use the following command:

check program.txt -o program.res

where program.txt is the input set of rules and "-o output.res" specifies the output file name. The tool runs the chase termination algorithm for simple linear rules, and returns whether the chase terminates along with the following additional statistics about the program in the output file:

- terminates: true if the chase terminats; false otherwise.
- avg_arity, max_arity, min_arity: The average, maximum, and minimum arities of the predicates.
- n_rules, n_exist_vars, n_predicates: The number of rules, predicates, and the existential variables in the rules.
- n_nodes, n_edges, n_components, n_spacial_components, n_special_edges: The number of nodes, edges, fully connected components, and fully connected components with special edges.
- t_parse: The time to parse the input file.
- t_graph: The time to build the dependency graph.
- t_comp: The time to find special fully connected components and check for their support.
- t_terminate: The end-to-end time to check termination.

