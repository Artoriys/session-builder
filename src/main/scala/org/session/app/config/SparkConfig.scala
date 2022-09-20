package org.session.app.config

import pureconfig.ConfigSource

case class SparkConfig(
                        defaultParallelism: Int,
                        driverHost: String,
                        customParams: Map[String, String]
                      )

object SparkConfig {

  import pureconfig.generic.auto._

  lazy val instance = ConfigSource.default.at("session.spark").loadOrThrow[SparkConfig]
}