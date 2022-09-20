//package org.session.app
//import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
//import org.apache.spark.sql.expressions.Window
//import org.session.app.model.input.Event
//import org.apache.spark.sql.functions._
//import org.session.app.config.JobContext
//import org.session.app.model.interim.Interim
//import org.session.app.model.output.OutputEvent
//
//import java.time.LocalDate
//import scala.annotation.tailrec
//
//object SessionJob extends SparkDriver {
//  override def transform(spark: SparkSession, jobContext: JobContext): Unit = {
//    import spark.implicits._
//
//    val inc = readIncrementalData(jobContext, spark)
//
//    val snapshot = readSnapshotData(jobContext, spark)
//
//    val union = snapshot
//      .drop($"session_id")
//      .as[Event]
//      .union(inc)
//
//
//    val window = Window
//      .partitionBy($"user_id", $"product_code")
//      .orderBy(
//        //$"user_id".asc,
//        //$"product_code".desc,
//        $"timestamp".asc
//      )
//
//  //  val ses_window = session_window($"timestamp", $"time_lag_sec")
//
//    val condition = when($"event_id".isin("a", "b", "c").and($"time_lag".isNull.or($"time_lag" > jobContext.lookbackTimeSec).or($"time_lag" === 0)),
//      concat_ws("#", $"user_id",  $"product_code", $"timestamp"))
//
//
//    val sessionsDf = union
//      .withColumn("unix_timestamp", unix_timestamp($"timestamp"))
//      .withColumn("time_lag", $"unix_timestamp" - lag($"unix_timestamp", 1).over(window))
//      .drop("unix_timestamp")
//      .withColumn("session_id", condition)
//      //.withColumn("time_lag_sec", concat($"time_lag", lit(" seconds")))
//      .na.fill(0, Seq("time_lag"))
//      //.withColumn("test_session", ses_window)
//      .as[Interim]
//
//      .groupByKey(v => (v.user_id, v.product_code))
//      .flatMapGroups {
//        case (_, events) =>
////          separateSessions(events.toList).flatMap { m =>
////            val (k, v) = m
////
////            if (k == null) v
////            else v.map(_.copy(session_id = k))
////          }
////          var last_session = Option.empty[String]
////          val ev = events.toVector
////
////          ev.map { e =>
////            last_session = if (e.session_id.nonEmpty) e.session_id else last_session
////
////            e.copy(session_id = last_session)
////
////          }
//          events
//
//
//      }
//
//    //writeData(sessionsDf.toDF())
//  }
//
//  def readIncrementalData(jobContext: JobContext, spark: SparkSession): Dataset[Event] = {
//    import spark.implicits._
//
//    spark
//      .read
//      .option("header", "true")
//      .csv(s"${jobContext.readPath}/${jobContext.readDate}")
//      .withColumn("timestamp", to_timestamp($"timestamp", jobContext.timestampFormat))
//      .as[Event]
//  }
//
//  def readSnapshotData(jobContext: JobContext, spark: SparkSession) = {
//    val currentDate = LocalDate.parse(jobContext.readDate)
//    import spark.implicits._
//
//    spark.emptyDataset[OutputEvent]
//      .filter(_.timestamp.toLocalDateTime.toLocalDate.isAfter(currentDate.minusDays(5)))
//  }
//
////  def separateSessions(events: List[Interim]): Map[Option[String], List[Interim]] = {
////    @tailrec
////    def iterate(eventsSessions: List[Interim], acc: Map[Option[String], List[Interim]]): Map[Option[String], List[Interim]] = {
////      eventsSessions match {
////        case Nil => acc
////        case head :: tail if head == null => iterate(tail, acc + (head.session_id -> tail))
////        case head :: tail if head != null =>
////          val session = tail.takeWhile(_.session_id == null)
////          iterate(tail.dropWhile(_.session_id == null), acc + (head.session_id -> session))
////      }
////    }
////    iterate(events, Map.empty)
////  }
//
//
////    def separateSessions(events: List[Interim]): Map[Option[String], List[Interim]] = {
////
////      for ()
////    }
//
//  def writeData(df: DataFrame) = {
//    import df.sparkSession.implicits._
//    df.orderBy($"user_id".asc, $"product_code".desc, $"timestamp".asc).show(truncate = false)
//  }
//
//}
