package org.session.app.calc

import org.apache.spark.sql.Dataset
import org.session.app.config.JobContext
import org.session.app.model.interim.InterimEvent
import org.session.app.model.output.OutputEvent
import org.session.app.utils.Constants
import org.apache.spark.sql.functions._

object SessionCalculation {


  def enrichWithSessionId(input: Dataset[InterimEvent], jobContext: JobContext): Dataset[OutputEvent] = {
    import input.sparkSession.implicits._
    import org.session.app.utils.OptionUtils._

    input
      .groupByKey(v => (v.user_id, v.product_code))
      .flatMapGroups {
        case (_, events) =>
          var lastSession = Option.empty[String]
          events.map {
            //Detect start of the session
            case event if (event.time_lag.isEmpty || event.time_lag > jobContext.sessionBrakeTimeSec) &&
              jobContext.userEvents.contains(event.event_id) =>
              lastSession = Some(buildSession(event))
              event.copy(session_id = lastSession)
            //Continue session
            case event if event.time_lag <= jobContext.sessionBrakeTimeSec =>
              event.copy(session_id = lastSession)
            //Not a session start and not a continue
            case event =>
              lastSession = Option.empty[String]
              event
          }
      }
      .drop($"time_lag")
      .withColumn("start_date", to_date($"timestamp"))
      .as[OutputEvent]
  }

  private def buildSession(event: InterimEvent): String = {
    s"${event.user_id}#${event.product_code}#${event.timestamp.toLocalDateTime.format(Constants.OUTPUT_TIMESTAMP_FORMATTER)}"
  }

}
