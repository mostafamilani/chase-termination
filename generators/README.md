The following command invokes the data generator and creates a database with name "dbname" with "n_relations" relations each of which has "n_tuples" tuples in it. 

```
dbgenerate -U username -P password -d dbname -size n_tuples -min min_attr -max max_attr -dm dm_dize
```


"min_att" and "max_att" specify the minimum and the maximum number of attributes in each relation. The generator returns a file "dbname.txt" that contains schema information of the generated database. The relations have schema "P_i(c_1,c_2,...,c_k)" where i is in [1,n_relations] and k is randomly selected from the range [min_att,max_att]. The tuples in the relations are from a domain set {1,2,...,dmsize}.

<!--
createdb -U username -O ownername -E UTF8 -T template0 -l en_US.UTF-8 databasename
psql -U username -d databasename -f filename.sql
pg_restore -U postgres -C -d chasedb d.sql
-->

