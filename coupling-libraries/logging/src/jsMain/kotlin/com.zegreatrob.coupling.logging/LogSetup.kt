package com.zegreatrob.coupling.logging

import korlibs.time.DateFormat
import korlibs.time.DateTime
import kotlinx.serialization.Serializable
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel

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

fun DateTime.logFormat() = toString(DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"))

@Suppress("unused")
@JsName("initializeLogging")
fun initializeLogging(developmentMode: Boolean) {
    KotlinLoggingConfiguration.LOG_LEVEL = if (developmentMode) {
        KotlinLoggingLevel.DEBUG
    } else {
        KotlinLoggingLevel.INFO
    }

    KotlinLoggingConfiguration.FORMATTER = JsonFormatter()
}
