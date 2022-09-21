package org.session.app

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, SparkSession}
import org.session.app.calc.SessionCalculation
import org.session.app.config.JobContext
import org.session.app.model.input.Event
import org.session.app.model.interim.InterimEvent
import org.session.app.model.output.OutputEvent

import java.time.LocalDate

object SessionSparkJob extends SparkDriver {
  override def transform(spark: SparkSession, jobContext: JobContext): Unit = {

    import spark.implicits._

    val inc = readIncrementalData(jobContext, spark)

    val snapshot = readSnapshotData(jobContext, spark)

    val united = snapshot
      .drop($"session_id")
      .drop($"start_date")
      .as[Event]
      .union(inc)

    val window = Window
      .partitionBy($"user_id", $"product_code")
      .orderBy(
        $"timestamp".asc,
        $"event_id".asc
      )

    val out = united
      .withColumn("unix_timestamp", unix_timestamp($"timestamp"))
      .withColumn("time_lag", $"unix_timestamp" - lag($"unix_timestamp", 1).over(window))
      .drop("unix_timestamp")
      .withColumn("session_id", typedlit(Option.empty[String]))
      .as[InterimEvent]
      .transform(SessionCalculation.enrichWithSessionId(_, jobContext))

    writeData(out)
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
      .filter(
        _.start_date
          .toLocalDate
          .isAfter(currentDate.minusDays(jobContext.lookupDays)))
  }

  def writeData(df: Dataset[OutputEvent], test: Boolean = true): Unit = {
    import df.sparkSession.implicits._
    if (test) {
      df.orderBy($"user_id".asc, $"product_code".desc, $"timestamp".asc).show(100,truncate = false)
    } else {
      df.writeTo("session").overwritePartitions()
    }
  }
}
