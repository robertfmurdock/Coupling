package com.zegreatrob.coupling.action

import com.benasher44.uuid.Uuid
import com.zegreatrob.testmints.action.ActionPipe
import com.zegreatrob.testmints.action.async.SuspendAction
import io.github.oshai.kotlinlogging.KotlinLogging

private val theLogger by lazy { KotlinLogging.logger("ActionLogger") }

class LoggingActionPipe(override val traceId: Uuid) : ActionPipe, ActionLoggingSyntax {

    override val logger = theLogger

    override suspend fun <D, R> execute(dispatcher: D, action: SuspendAction<D, R>): R = try {
        logAsync(action) { super.execute(dispatcher, action) }
    } catch (bad: Throwable) {
        logger.error(bad) { "Error executing ${action::class.simpleName}" }
        throw bad
    }
}
