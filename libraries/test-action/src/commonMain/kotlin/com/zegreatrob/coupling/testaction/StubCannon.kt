package com.zegreatrob.coupling.testaction

import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.ActionWrapper
import com.zegreatrob.testmints.action.async.SuspendAction
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.reflect.KClass

object StubCannon {
    operator fun <D : Any> invoke(receivedActions: MutableList<Any?>) = Synchronous<D>(receivedActions)

    class Synchronous<D : Any>(val receivedActions: MutableList<Any?> = mutableListOf()) : ActionCannon<D> {

        private val immediateReturn = mutableMapOf<Any, Any?>()
        private val anyReturn = mutableListOf<Any?>()

        fun <R> given(action: SuspendAction<D, R>, returnValue: R) {
            immediateReturn[action] = returnValue
        }

        fun <R> given(action: SuspendAction<D, R>): Captor<D, R> = Captor(action)

        fun <A, R> givenAny(
            @Suppress("UNUSED_PARAMETER") actionType: KClass<A>,
            vararg returnValues: R,
        ) where A : SuspendAction<D, R> {
            anyReturn.addAll(returnValues)
        }

        override suspend fun <R> fire(action: SuspendAction<D, R>): R {
            val element = action.unwrap()
            receivedActions.add(element)

            return if (immediateReturn.containsKey(action)) {
                @Suppress("UNCHECKED_CAST")
                immediateReturn[action] as R
            } else if (anyReturn.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                anyReturn.removeFirst() as R
            } else {
                throw RuntimeException("No result prepared for action $element")
            }
        }

        private fun <R> SuspendAction<D, R>.unwrap() = if (this is ActionWrapper<*, *>) {
            action
        } else {
            this
        }

        inner class Captor<D, R>(val action: SuspendAction<D, R>) {
            fun thenReturn(value: R) {
                immediateReturn[action] = value
            }
        }
    }

    class Channel<D : Any>(
        private val actionChannel: SendChannel<Any>,
        private val resultChannel: ReceiveChannel<*>,
    ) : ActionCannon<D> {
        override suspend fun <R> fire(action: SuspendAction<D, R>): R {
            actionChannel.send(action)
            @Suppress("UNCHECKED_CAST")
            return resultChannel.receive() as R
        }
    }
}
