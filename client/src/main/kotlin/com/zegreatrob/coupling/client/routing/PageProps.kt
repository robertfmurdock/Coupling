package com.zegreatrob.coupling.client.routing

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
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
    fun getDispatcher(traceId: Uuid): CommandDispatcher

    private fun tracingDispatcher() = getDispatcher(uuid4())
    suspend fun <T> runQuery(dispatch: suspend CommandDispatcher.() -> T): T = tracingDispatcher().dispatch()

    fun buildCommandFunc(scope: CoroutineScope): CommandFunc<CommandDispatcher> = { runCommands ->
        { scope.launch { with(tracingDispatcher()) { runCommands() } } }
    }

}

object MasterCommander : Commander {
    override fun getDispatcher(traceId: Uuid): CommandDispatcher = CommandDispatcher(traceId)
}