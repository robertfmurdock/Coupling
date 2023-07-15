package com.zegreatrob.coupling.client.components

import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.async.SuspendAction

interface DispatchFunc<D> {
    operator fun <C : SuspendAction<D, R>, R> invoke(
        commandFunc: () -> C,
        response: (R) -> Unit,
    ): () -> Unit

    operator fun <C, R> invoke(
        commandFunc: () -> C,
        fireFunc: suspend ActionCannon<D>.(C) -> R,
        response: (R) -> Unit,
    ): () -> Unit
}
