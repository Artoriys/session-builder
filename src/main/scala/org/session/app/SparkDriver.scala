package org.session.app

import org.apache.spark.sql.SparkSession
import org.session.app.config.{JobContext, SparkConfig}

trait SparkDriver extends App {

  def run(): Unit = {
    val sparkConfig = SparkConfig.instance
    val sparkBuilder = SparkSession
      .builder()
      .appName("Session")
      .master(s"local[${sparkConfig.defaultParallelism}]")
      .config("spark.sql.shuffle.partitions", sparkConfig.defaultParallelism)
      .config("spark.default.parallelism", sparkConfig.defaultParallelism)

    val spark = sparkConfig.customParams.foldLeft(sparkBuilder) { (state, param) =>
      state.config(param._1, param._2)
    }.getOrCreate()

    transform(spark, JobContext.instance)
  }

  def transform(spark: SparkSession, jobContext: JobContext): Unit

  run()
}
