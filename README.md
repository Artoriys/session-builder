# Session builder

We had two approaches 
### 1. Simple
We had incremental data which comes every day and historical table (for example `session` table, 
or it can be just partitioned hdfs dataset) which partitioned by day, taken from `timestamp` column.
When daily job starts it will read daily incremental and latest 5 days from historical data.
After that we drop `session_id` (unfortunately we should do it because every session can change)
and union incremental data with part of historical data. After 
session calculation we overwrite partitions. It will affect only 5 latest partitions and will 
not touch all history.

### 2. Alternative
In case if we had some requirements for `session` table, for example no partitioning or append only.
We can have intermediate table which populated from incremental data and a job which reads 
data from intermediate table older when 5 days, append it to history table and deletes it from the intermediate 
table. Appending and deleting should be done in one transaction. Deleting is not supported by default Spark, 
so we can use Apache Iceberg https://iceberg.apache.org/docs/latest/spark-writes/#delete-from or Delta Lake 
https://docs.delta.io/latest/delta-update.html#delete-from-a-table.
