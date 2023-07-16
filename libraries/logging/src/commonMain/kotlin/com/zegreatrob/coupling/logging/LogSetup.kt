package com.zegreatrob.coupling.logging

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val level: String,
    val name: String,
    val message: String?,
    val properties: Map<String, String?>?,
    val timestamp: String,
    val marker: String? = null,
    val stackTrace: List<String>? = null,
)

fun LocalDateTime.logFormat() = toString()
