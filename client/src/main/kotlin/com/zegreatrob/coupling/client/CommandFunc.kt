package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.action.CommandExecuteSyntax
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface DispatchFunc<D> {
    operator fun <C : SuspendAction<D, R>, R> invoke(commandFunc: () -> C, response: (Result<R>) -> Unit): () -> Unit
}

class DecoratedDispatchFunc<D : ActionLoggingSyntax>(
    val dispatcherFunc: () -> D,
    private val scope: CoroutineScope
) : DispatchFunc<D>, CommandExecuteSyntax {

    override fun <C : SuspendAction<D, R>, R> invoke(commandFunc: () -> C, response: (Result<R>) -> Unit): () -> Unit =
        {
            scope.launch {
                dispatcherFunc()
                    .execute(commandFunc())
                    .let(response)
            }
        }

}
