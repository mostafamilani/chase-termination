# Overview

This repository has a tool to check if the chase process finishes when using linear and simple linear rules. It's based on the methods from the paper **"Semi-Oblivious Chase Termination for Linear Existential Rules: An Experimental Study"**.[^1] The repository also offers a data generator and a rule generator to help test the tool, plus examples of data and rules made with these generators.

What's inside the repository:
- `scenarios` has examples of data and rules with explanations.
- `generators` includes the tools to create data and rules, with a short guide on how to use them.
- `code` contains all the code needed to run the tool for checking if the chase process with linear and simple-linear rules ends.

## Checking Chase Termination 

This work focuses on a type of algorithms based on acyclicity checking in the dependency graph of rules. To run the algorithms for a set of rules "rules.txt", use the following command:

```
java -jar chase-termination.jar -f input-file [-l -d dbconfig] [-o output.txt]
```


Options explained:
- `-f`: Specifies the path of the ontology input file for the chase termination process. This option is required.
- `-o`: (Optional) Outputs to the specified file and its path. The default output file is named `input-file.res`, located in the same directory as the input file.
- `-l`: (Optional) Enables checking for linear rules. By default, the tool checks for simple linear rules.
- `-d`: Specifies the database configuration file. This is used for operations requiring database access.
- `-ln`: (Optional) Removes non-linear rules before checking for termination.

When `-l` is used, the tool checks for linear rules and requires a database configuration file specified by `-d dbconfig`. If `-l` is not included, implying the check is for simple-linear rules, the database information becomes optional. The tool assumes there's at least one tuple in the database for each relation if no database is specified.

To change the output file name from the default, use the `-o` option followed by your preferred file name. For example, `-o output.res` changes the output file name to `output.res`. The default behavior is to name the output file `input-file.res` based on the input file name.

The tool runs the chase termination algorithm and tells you if the chase will stop. The output file also gives statistics about the run:

- `terminates`: `true` if the chase stops; `false` if it doesn’t.
- `avg_arity`, `max_arity`, `min_arity`: Average, maximum, and minimum numbers arity of the predicates.
- `n_rules`, `n_predicates`, `n_exist_vars`: Counts of rules, predicates, and existential variables in the rules.
- `n_nodes`, `n_edges`, `n_special_components`, `n_special_edges`: Counts of nodes, edges, special strongly connected components, and special edges in the rule set's dependency graph.
- `t_parse`: Time it takes to read the rules from the input file.
- `t_graph`: Time to make the dependency graph.
- `t_comp`: Time to identify strongly connected components with a special edge and to check their support.
- `t_terminate`: Total time from start to finish to check if the chase stops.

For linear rules, which include dynamic simplification, the tool gives extra stats related to this process:
- `n_facts`, `n_shapes`: Numbers of facts and shapes in the database.
- `t_shapes`: Time to find shapes.

[^1]: Calautti, Marco, Mostafa Milani, and Andreas Pieris. "Semi-Oblivious Chase Termination for Linear Existential Rules: An Experimental Study." arXiv preprint arXiv:2303.12851 (2023).
