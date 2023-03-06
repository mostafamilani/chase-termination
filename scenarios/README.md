## Evaluation Scenarios

This repository provides data for two types of scenarios used in our experimental evaluation:

1. *Synthetic scenarios*: The repository includes scenarions with 45 sets of linear rules and 900 sets of simple-linear rules that are generated using our rule generator. The sets of rules are categorized into nine profiles based on three rule profiles in the ranges [1,333K], [333K,666K], and [666K,1M], and three predicate profiles with sets of rules having [5,200], [200,400], and [400,600] predicates. Each of the nine profiles contains five sets of linear rules, and there are 100 sets for simple-linear rules. The evaluation for simple-linear rules is conducted without any extensional database and assumes all predicates have extensional data. The experiments with linear rules use a database with its backup dump shared in this repository.
2. *Scenarios based on existing benchmarks*: These scenarios are frequently used in the literature and include the Deep rules family, the LUBM rules family with four databases of varying sizes, and the iBench benchmarks, which comprise STB-128 and ONT-256. These evaluation scenarios utilize extensional databases with their backup dumps also available in this repository.

All scenarios' data and rules are available at https://shorturl.at/yzHJ6.

<!-- https://bit.ly/41KCA5I.-->
