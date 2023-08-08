package io.acay.fpl.model.sqlite

import java.sql.Timestamp

class Notification(
    val id: Int,
    val title: String,
    val description: String,
    val timestamp: String,
    val seen: Boolean
)