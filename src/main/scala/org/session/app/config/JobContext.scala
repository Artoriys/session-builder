package org.session.app.config

import pureconfig.ConfigSource

case class JobContext(
                     readDate: String,
                     readPath: String,
                     sessionBrakeTimeSec: Int,
                     lookupDays: Int = 5,
                     timestampFormat: String,
                     userEvents: Seq[String] = Seq("a", "b", "c")
                     )

object JobContext {

  import pureconfig.generic.auto._

  lazy val instance = ConfigSource.default.at("session.context").loadOrThrow[JobContext]
}
