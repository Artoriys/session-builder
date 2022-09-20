package org.session.app.model.output

import java.sql.Timestamp

case class OutputEvent(
                        user_id: String,
                        event_id: String,
                        timestamp: Timestamp,
                        product_code: String,
                        session_id: String
                      )
