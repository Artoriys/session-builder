package org.session.app

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{lag, to_timestamp, typedlit, unix_timestamp}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.session.app.config.JobContext
import org.session.app.model.input.Event
import org.session.app.model.interim.Interim
import org.session.app.model.output.OutputEvent

import java.time.LocalDate

object SessionSparkJob extends SparkDriver {
  override def transform(spark: SparkSession, jobContext: JobContext): Unit = {

    import spark.implicits._

    val inc = readIncrementalData(jobContext, spark)

    val snapshot = readSnapshotData(jobContext, spark)

    val union = snapshot
      .drop($"session_id")
      .as[Event]
      .union(inc)

    val window = Window
      .partitionBy($"user_id", $"product_code")
      .orderBy(
        $"timestamp".asc
      )


    val out = union
      .withColumn("unix_timestamp", unix_timestamp($"timestamp"))
      .withColumn("time_lag", $"unix_timestamp" - lag($"unix_timestamp", 1).over(window))
      .drop("unix_timestamp")
      .withColumn("session_id", typedlit(Option.empty[String]))
      .na.fill(0, Seq("time_lag"))
      .as[Interim]
      .groupByKey(v => (v.user_id, v.product_code))
      .flatMapGroups {
        case (_, events) =>
          var last_session = Option.empty[String]
          events.map {
                //Detect start of the session
            case event if (event.time_lag == 0 ||
              event.time_lag > jobContext.lookbackTimeSec) &&
              jobContext.userEvents.contains(event.event_id) =>
              last_session = Some(s"${event.user_id}#${event.product_code}#${event.timestamp}")
              event.copy(session_id = last_session)
              //Continue session
            case event if event.time_lag <= jobContext.lookbackTimeSec =>
              event.copy(session_id = last_session)
              //Not a session start
            case event =>
              last_session = Option.empty[String]
              event
          }
      }

    writeData(out.toDF())
  }

  def readIncrementalData(jobContext: JobContext, spark: SparkSession): Dataset[Event] = {
    import spark.implicits._

    spark
      .read
      .option("header", "true")
      .csv(s"${jobContext.readPath}/${jobContext.readDate}")
      .withColumn("timestamp", to_timestamp($"timestamp", jobContext.timestampFormat))
      .as[Event]
  }

  def readSnapshotData(jobContext: JobContext, spark: SparkSession) = {
    val currentDate = LocalDate.parse(jobContext.readDate)
    import spark.implicits._

    spark.emptyDataset[OutputEvent]
      .filter(_.timestamp.toLocalDateTime.toLocalDate.isAfter(currentDate.minusDays(5)))
  }

  def writeData(df: DataFrame) = {
    import df.sparkSession.implicits._
    df.orderBy($"user_id".asc, $"product_code".desc, $"timestamp".asc).show(truncate = false)
  }
}
