package com.zegreatrob.coupling.logging

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
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
    val stackTrace: List<String>? = null
)

fun DateTime.logFormat() = toString(DateFormat.FORMAT1)

@OptIn(UnstableDefault::class)
@Suppress("unused")
@JsName("initializeJasmineLogging")
fun initializeJasmineLogging(developmentMode: Boolean) {
    KotlinLoggingConfiguration.LOG_LEVEL = if (developmentMode) {
        KotlinLoggingLevel.DEBUG
    } else {
        KotlinLoggingLevel.INFO
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    KotlinLoggingConfiguration.FORMATTER = JsonFormatter()
}

