# Session builder

We had two approaches 
### 1. Simple
We had incremental data which comes every day and historical table (for example `session` table, 
or it can be just partitioned hdfs dataset) which partitioned by day, taken from `timestamp` column.
When daily job starts it will read all incremental and latest 5 days from historical data.
After that we drop `session_id` and union incremental data with part of historical data. After
 session calculation we overwrite partitions. It will affect only 5 latest partitions and do 
not touch all history.
