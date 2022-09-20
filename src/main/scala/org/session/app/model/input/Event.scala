package org.session.app.model.input

import java.sql.Timestamp

case class Event(
                user_id: String,
                event_id: String,
                timestamp: Timestamp,
                product_code: String
                )
