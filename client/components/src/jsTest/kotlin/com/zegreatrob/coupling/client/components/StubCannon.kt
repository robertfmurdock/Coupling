package com.zegreatrob.coupling.client.components

import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.ActionWrapper
import com.zegreatrob.testmints.action.async.SuspendAction
import kotlinx.coroutines.channels.Channel

class StubCannon<D>(
    private val receivedActions: MutableList<Any?>,
    private val resultChannel: Channel<*>,
) : ActionCannon<D> {

    override suspend fun <R> fire(action: SuspendAction<D, R>): R {
        if (action is ActionWrapper<*>) {
            receivedActions.add(action.action)
        } else {
            receivedActions.add(action)
        }
        @Suppress("UNCHECKED_CAST")
        return resultChannel.receive() as R
    }
}
