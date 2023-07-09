package com.zegreatrob.coupling.client.components

import com.zegreatrob.testmints.action.async.SuspendAction

interface DispatchFunc<D> {
    operator fun <C : SuspendAction<D, R>, R> invoke(
        commandFunc: () -> C,
        response: (R) -> Unit,
    ): () -> Unit
}
