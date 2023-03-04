The following commands invokes the data generator and creates a database with the given specification...  

```
createdb -U username -O ownername -E UTF8 -T template0 -l en_US.UTF-8 databasename
psql -U username -d databasename -f filename.sql
```
