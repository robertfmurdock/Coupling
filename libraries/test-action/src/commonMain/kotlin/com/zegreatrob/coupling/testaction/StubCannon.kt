package com.zegreatrob.coupling.testaction

import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.ActionWrapper
import com.zegreatrob.testmints.action.async.SuspendAction
import kotlinx.coroutines.channels.ReceiveChannel

class StubCannon<D>(
    private val receivedActions: MutableList<Any?>,
    private val resultChannel: ReceiveChannel<*>,
) : ActionCannon<D> {

    val immediateReturn = mutableMapOf<Any, Any?>()

    override suspend fun <R> fire(action: SuspendAction<D, R>): R {
        println("fire $action")
        val element = action.unwrap()
        receivedActions.add(element)

        if (immediateReturn.containsKey(element)) {
            @Suppress("UNCHECKED_CAST")
            return immediateReturn[element] as R
        } else {
            @Suppress("UNCHECKED_CAST")
            return resultChannel.receive() as R
        }
    }

    private fun <R> SuspendAction<D, R>.unwrap() =
        if (this is ActionWrapper<*>) {
            action
        } else {
            this
        }
}
