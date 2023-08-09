package io.acay.fpl.model.sqlite

class Notification(
    val id: Int,
    val title: String,
    val description: String,
    val timestamp: String,
    var seen: Boolean
)