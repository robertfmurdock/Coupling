package com.zegreatrob.coupling.client.routing

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.client.LocalStorageRepositoryBackend
import com.zegreatrob.coupling.client.memory.MemoryRepositoryCatalog
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.couplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.testmints.action.ActionCannon
import js.core.ReadonlyRecord
import kotlinx.browser.window
import kotlinx.datetime.Clock
import org.w3c.dom.get
import react.Props
import web.url.URLSearchParams

external interface PageProps : Props {
    var pathParams: ReadonlyRecord<String, String>
    var commander: Commander
    var search: URLSearchParams
}

val PageProps.partyId: PartyId? get() = pathParams["partyId"]?.let(::PartyId)
val PageProps.playerId: String? get() = pathParams["playerId"]
val PageProps.pinId: String? get() = pathParams["pinId"]

interface Commander {
    fun getDispatcher(traceId: Uuid): ActionCannon<CouplingSdkDispatcher>
    fun tracingCannon() = getDispatcher(uuid4())
}

class MasterCommander(private val getIdentityToken: suspend () -> String) : Commander {
    private val backend = LocalStorageRepositoryBackend()
    override fun getDispatcher(traceId: Uuid) = if (window["inMemory"] == true) {
        ActionCannon(MemoryRepositoryCatalog("test-user", backend, Clock.System))
    } else {
        couplingSdk(getIdentityToken, defaultClient(getLocationAndBasename(), traceId), LoggingActionPipe(traceId))
    }
}

fun getLocationAndBasename(): Pair<String, String> {
    val location = window.location.origin
    val basename = "${window["basename"]}"
    return location to basename
}
