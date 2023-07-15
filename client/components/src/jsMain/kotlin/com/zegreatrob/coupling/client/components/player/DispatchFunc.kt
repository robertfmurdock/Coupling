package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.testmints.action.ActionCannon

fun interface DispatchFunc<T> {
    operator fun invoke(block: suspend ActionCannon<T>.() -> Unit): () -> Unit
}

fun <T> dispatchFunc(function: (suspend ActionCannon<T>.() -> Unit) -> () -> Unit) = DispatchFunc(function)
