package com.zegreatrob.coupling.client.routing

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.model.tribe.TribeId
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

    fun tracingDispatcher() = getDispatcher(uuid4())
    suspend fun <T> runQuery(dispatch: suspend CommandDispatcher.() -> T): T = tracingDispatcher().dispatch()

}

object MasterCommander : Commander {
    override fun getDispatcher(traceId: Uuid): CommandDispatcher = CommandDispatcher(traceId)
}