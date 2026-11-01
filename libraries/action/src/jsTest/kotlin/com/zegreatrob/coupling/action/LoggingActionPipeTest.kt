package com.zegreatrob.coupling.action

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.async.asyncSetup
import io.github.oshai.kotlinlogging.Appender
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.uuid.Uuid

class LoggingActionPipeTest {
    @Test
    fun cancellationPropagatesWithoutLoggingAnError() = asyncSetup(object {
        val failure = CancellationException("request cancelled")
        val events = mutableListOf<KLoggingEvent>()
        val originalAppender = KotlinLoggingConfiguration.direct.appender
        val pipe = LoggingActionPipe(Uuid.random())
    }) {
        KotlinLoggingConfiguration.direct.appender = object : Appender {
            override fun log(loggingEvent: KLoggingEvent) {
                events.add(loggingEvent)
            }
        }
    } exercise {
        try {
            runCatching { pipe.execute(Unit, ThrowingAction(failure)) }
        } finally {
            KotlinLoggingConfiguration.direct.appender = originalAppender
        }
    } verify { result ->
        result.exceptionOrNull().assertIsEqualTo(failure)
        events.filter { it.level == Level.ERROR }.assertIsEqualTo(emptyList())
        events.filter { it.message?.contains("command_phase=end") == true }
            .map { it.message?.contains("command_cancelled=true") }
            .assertIsEqualTo(listOf(true))
        events.any { it.message?.contains("command_error=true") == true }.assertIsEqualTo(false)
    }

    @Test
    fun ordinaryFailureIsLoggedAndPropagates() = asyncSetup(object {
        val failure = IllegalStateException("request failed")
        val events = mutableListOf<KLoggingEvent>()
        val originalAppender = KotlinLoggingConfiguration.direct.appender
        val pipe = LoggingActionPipe(Uuid.random())
    }) {
        KotlinLoggingConfiguration.direct.appender = object : Appender {
            override fun log(loggingEvent: KLoggingEvent) {
                events.add(loggingEvent)
            }
        }
    } exercise {
        try {
            runCatching { pipe.execute(Unit, ThrowingAction(failure)) }
        } finally {
            KotlinLoggingConfiguration.direct.appender = originalAppender
        }
    } verify { result ->
        result.exceptionOrNull().assertIsEqualTo(failure)
        events.filter { it.level == Level.ERROR }.map { it.cause }.assertIsEqualTo(listOf(failure))
    }
}

private class ThrowingAction(private val failure: Throwable) : SuspendAction<Unit, Unit> {
    override suspend fun execute(dispatcher: Unit): Unit = throw failure
}
