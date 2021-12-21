package com.zegreatrob.coupling.client.routing

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.LocalStorageRepositoryBackend
import com.zegreatrob.coupling.client.MemoryRepositoryCatalog
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.SdkSingleton
import com.zegreatrob.minreact.DataProps
import kotlinext.js.Record
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.url.URLSearchParams

data class PageProps(
    val pathParams: Record<String, String>,
    val commander: Commander,
    val search: URLSearchParams
) : DataProps {
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
    private val backend = LocalStorageRepositoryBackend()

    override fun getDispatcher(traceId: Uuid): CommandDispatcher = CommandDispatcher(
        traceId,
        if (window["inMemory"] == true) {
            MemoryRepositoryCatalog("test-user", backend, TimeProvider)
        } else {
            SdkSingleton
        }
    )
}
