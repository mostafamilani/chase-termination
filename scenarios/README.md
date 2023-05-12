## Evaluation Scenarios

This repository provides data for two types of scenarios used in our experimental evaluation:

1. *OWL ontologies*: We processed a repository of 797 OWL ontologies and transformed their TBox axioms to tgds with single atom heads to evaluate our chase termintion algorithms.  These ontologies  include  a  subset  of  the  Gardiner  ontology  corpus,[^1] several  Phenoscape  ontologies,  and  a  number  of ontologies from two versions of the Open Biomedical Ontology (OBO) corpus. For the algorithm for linear tgds, we construct a database with multiple schemas each of which contains ABox of one of the OWL ontologies.
2. *Synthetic scenarios*: The repository includes scenarions with 45 sets of linear rules and 900 sets of simple-linear rules that are generated using our rule generator. The sets of rules are categorized into nine profiles based on three rule profiles in the ranges [1,333K], [333K,666K], and [666K,1M], and three predicate profiles with sets of rules having [5,200], [200,400], and [400,600] predicates. Each of the nine profiles contains five sets of linear rules, and there are 100 sets for simple-linear rules. The evaluation for simple-linear rules is conducted without any extensional database and assumes all predicates have extensional data. The experiments with linear rules use a database with its backup dump shared in this repository.
3. *Scenarios based on existing benchmarks*: These scenarios are frequently used in the literature and include the Deep rules family, the LUBM rules family with four databases of varying sizes, and the iBench benchmarks, which comprise STB-128 and ONT-256. These evaluation scenarios utilize extensional databases with their backup dumps also available in this repository.

All scenarios' data and rules are available at our dataset repository.[^2]

<!-- https://bit.ly/41KCA5I.-->

[^1]: Gardiner, T., Tsarkov, D., & Horrocks, I. (2006). Framework for an Automated Comparisonof Description Logic Reasoners.  ISWC  2006, Vol. 4273, pp. 654–667.
[^2]: https://shorturl.at/dguFU

