package org.session.app.model.interim

import java.sql.Timestamp

case class Interim(
                    user_id: String,
                    event_id: String,
                    timestamp: Timestamp,
                    product_code: String,
                    time_lag: Option[Long],
                    session_id: Option[String]
                  )
