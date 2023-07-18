package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.testaction.StubCannon
import kotlinx.coroutines.channels.Channel

class StubDispatcher {
    val receivedActions = mutableListOf<Any?>()
    val resultChannel = Channel<Any>()
    fun <T> func() = stubDispatchFunc<T>(StubCannon(receivedActions, resultChannel))
}
