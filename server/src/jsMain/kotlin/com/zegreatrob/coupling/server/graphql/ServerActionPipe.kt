package com.zegreatrob.coupling.server.graphql

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.testmints.action.ActionPipe
import com.zegreatrob.testmints.action.async.SuspendAction

class ServerActionPipe(override val traceId: Uuid) : ActionPipe, ActionLoggingSyntax {
    override suspend fun <D, R> execute(dispatcher: D, action: SuspendAction<D, R>): R {
        try {
            return logAsync(action) { super.execute(dispatcher, action) }
        } catch (bad: Throwable) {
            logger.error(bad) { "Error executing ${action::class.simpleName}" }
            throw bad
        }
    }
}
