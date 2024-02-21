## Evaluation Scenarios

This repository provides data for 3 types of scenarios used in our experimental evaluation:

1. *Real-world scenarios*: We used a repository of OWL ontologies to evaluate our acyclicity-based termination algorithm and also compare it with the materialization-based algorithm as our baseline. To this end, we transformed the TBoxes of these ontologies into sets of linear existential rules with a single atom in their heads to be able to run our algorithm. These ontologies consist of real-world scenarios and include a subset of the Gardiner ontology corpus,[^1] several Phenoscape ontologies, and a number of ontologies from two versions of the Open Biomedical Ontology (OBO) corpus. From the 797 ontologies in this repository, we used 483 for running both materialization- and acyclicity-based algorithms as the rest of the ontologies do not have any ABoxes. To convert the ABoxes to a database, one for each ABox. See[^2] for more details about these ontologies and the results of running our algorithms using these ontologies.
2. *Synthetic scenarios*: The repository includes scenarios with 45 sets of linear rules and 900 sets of simple-linear rules that are generated using our rule generator. The sets of rules are categorized into nine profiles based on three rule profiles in the ranges [1,333K], [333K,666K], and [666K,1M], and three predicate profiles with sets of rules having [5,200], [200,400], and [400,600] predicates. Each of the nine profiles contains five sets of linear rules, and there are 100 sets of simple linear rules. The evaluation for simple-linear rules is conducted without any database and assumes that each  predicate contains at least one tuple in the database. The experiments with linear rules use a database with its backup dump shared in this repository.
3. *Scenarios based on existing benchmarks*: These scenarios are frequently used in the literature and include the Deep rules family, the LUBM rules family with four databases of varying sizes, and the iBench benchmarks, which comprise STB-128 and ONT-256. These evaluation scenarios utilize databases with their backup dumps also available in this repository.

All scenarios' data and rules are available in [our dataset repository](https://uwoca-my.sharepoint.com/:f:/g/personal/mmilani7_uwo_ca/Ek6-Z3cDT-RMkGzjgFGs5kgBNPRwQAb5dyLlcVF3dWo3fQ?e=1VZTbK).

<!-- https://bit.ly/41KCA5I.-->

[^1]: http://krr-nas.cs.ox.ac.uk/ontologies/UID/
[^2]: Gardiner, T., Tsarkov, D., & Horrocks, I. (2006, November). Framework for an automated comparison of description logic reasoners. In International semantic web conference (pp. 654-667).
[^3]: Grau, B. C., Horrocks, I., Krötzsch, M., Kupke, C., Magka, D., Motik, B., & Wang, Z. (2013). Acyclicity notions for existential rules and their application to query answering in ontologies. Journal of Artificial Intelligence Research, 47, 741-808.

