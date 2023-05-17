## Evaluation Scenarios

This repository provides data for two types of scenarios used in our experimental evaluation:

1. *Real-world ontologies*: We used a repository of OWL ontologies[^1] to evaluate our acyclicity-based termination algorithm and also compare it with the chase-based algorithm as our baseline. To this end, we transformed the TBoxes of these ontologies to sets of simple-linear existential rules with single atom in their heads to be able to run our algorithm. These ontologies consists of real-world scenarios and include a subset of the Gardiner ontology corpus,[^2] several Phenoscape ontologies, and a number of ontologies from two versions of the Open Biomedical Ontology (OBO) corpus. From the 797 ontologies in this repository, we used 712 for running both chase-based and acyclicicty based algorithms as the rest of the ontologies do not have any ABoxes. To convert the ABoxes to extensional database for existentional rules, we create a database with multiple schemas, where each schema contains the ABox of one of the OWL ontologies. See[^3] for more detail about these ontologies and the results of running our algorithms using these ontologies.
2. *Synthetic scenarios*: The repository includes scenarions with 45 sets of linear rules and 900 sets of simple-linear rules that are generated using our rule generator. The sets of rules are categorized into nine profiles based on three rule profiles in the ranges [1,333K], [333K,666K], and [666K,1M], and three predicate profiles with sets of rules having [5,200], [200,400], and [400,600] predicates. Each of the nine profiles contains five sets of linear rules, and there are 100 sets for simple-linear rules. The evaluation for simple-linear rules is conducted without any extensional database and assumes all predicates have extensional data. The experiments with linear rules use a database with its backup dump shared in this repository.
3. *Scenarios based on existing benchmarks*: These scenarios are frequently used in the literature and include the Deep rules family, the LUBM rules family with four databases of varying sizes, and the iBench benchmarks, which comprise STB-128 and ONT-256. These evaluation scenarios utilize extensional databases with their backup dumps also available in this repository.

All scenarios' data and rules are available at our dataset repository.[^4]

<!-- https://bit.ly/41KCA5I.-->

[^1]: http://krr-nas.cs.ox.ac.uk/ontologies/UID/
[^2]: Gardiner, T., Tsarkov, D., & Horrocks, I. (2006). Framework for an Automated Comparisonof Description Logic Reasoners.  ISWC  2006, Vol. 4273, pp. 654–667.
[^3]: Grau, B. Cuenca, et al. Acyclicity notions for existential rules and their application to query answering in ontologies. JAIR, 47 (2013): 741-808.
[^4]: https://shorturl.at/dguFU

