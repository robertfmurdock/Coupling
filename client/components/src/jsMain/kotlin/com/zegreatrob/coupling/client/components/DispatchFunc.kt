package com.zegreatrob.coupling.client.components

import com.zegreatrob.testmints.action.ActionCannon

fun interface DispatchFunc<out T> {
    operator fun invoke(block: suspend ActionCannon<T>.() -> Unit): () -> Unit
}

fun <T> dispatchFunc(function: (suspend ActionCannon<T>.() -> Unit) -> () -> Unit) = DispatchFunc(function)
