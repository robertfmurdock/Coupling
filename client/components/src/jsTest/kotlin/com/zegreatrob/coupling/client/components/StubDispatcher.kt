package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.testmints.action.ActionWrapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow

object StubDispatcher {

    operator fun invoke() = SynchronousStubDispatcher()

    class SynchronousStubDispatcher {
        val receivedActions = mutableListOf<Any?>()
        fun <T> func() = stubDispatchFunc<T>(StubCannon.Synchronous(receivedActions))
    }

    class Channel {
        val resultChannel = Channel<Any?>()
        val actionChannel = Channel<Any>()

        fun <T> func() = stubDispatchFunc<T>(
            StubCannon.Channel(
                actionChannel = actionChannel,
                resultChannel,
            ),
        )

        suspend inline fun onActionReturn(returnValue: Any): Any =
            actionChannel.receiveAsFlow()
                .filterIsInstance<ActionWrapper<*, *>>()
                .first()
                .action
                .also { resultChannel.send(returnValue) }
    }
}
