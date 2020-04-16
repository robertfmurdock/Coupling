package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.CommandFunc
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.url.URLSearchParams
import react.RProps

data class PageProps(
    val pathParams: Map<String, String>,
    val pathSetter: (String) -> Unit,
    val commander: Commander,
    val search: URLSearchParams
) : RProps {
    val tribeId: TribeId? get() = pathParams["tribeId"]?.let(::TribeId)
    val playerId: String? get() = pathParams["playerId"]
    val pinId: String? get() = pathParams["pinId"]
}

interface Commander {
    val dispatcher: CommandDispatcher
    fun <T> suspendFunc(dispatch: suspend CommandDispatcher.() -> T): suspend () -> T = { dispatcher.dispatch() }

    fun buildCommandFunc(scope: CoroutineScope): CommandFunc<CommandDispatcher> = { runCommands ->
        { scope.launch { with(dispatcher) { runCommands() } } }
    }

}

object MasterCommander : Commander {
    override val dispatcher: CommandDispatcher = CommandDispatcher
}