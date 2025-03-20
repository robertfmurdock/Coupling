package com.zegreatrob.coupling.client.components.graphing

fun scaledTimeFormat(min: Long, max: Long): String {
    val range = max - min
    val hasMinutes = (range / (1000 * 60)) > 1
    val hasHours = (range / (1000 * 60 * 60)) > 1
    val hasDays = (range / (1000 * 60 * 60 * 24)) > 1
    val hasMonths = (range / (1000 * 60 * 60 * 24 * 30)) > 1

    return when {
        hasMonths -> "%y-%m-%d"
        hasDays -> "%m-%d"
        hasHours -> "%H:%M"
        hasMinutes -> "%H:%M:%S"
        else -> "%H:%M:%S.%L"
    }
}
