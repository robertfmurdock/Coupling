package com.zegreatrob.coupling.client.routing

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.LocalStorageRepositoryBackend
import com.zegreatrob.coupling.client.MemoryRepositoryCatalog
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.SdkSingleton
import com.zegreatrob.coupling.sdk.defaultClient
import kotlinx.browser.window
import kotlinx.js.Record
import kotlinx.js.get
import org.w3c.dom.get
import org.w3c.dom.url.URLSearchParams
import react.Props

external interface PageProps : Props {
    var pathParams: Record<String, String>
    var commander: Commander
    var search: URLSearchParams
}

val PageProps.partyId: PartyId? get() = pathParams["partyId"]?.let(::PartyId)
val PageProps.playerId: String? get() = pathParams["playerId"]
val PageProps.pinId: String? get() = pathParams["pinId"]

interface Commander {
    fun getDispatcher(traceId: Uuid): CommandDispatcher
    fun tracingDispatcher() = getDispatcher(uuid4())
}

class MasterCommander(getIdentityToken: suspend () -> String) : Commander {
    private val backend = LocalStorageRepositoryBackend()
    private val sdk = SdkSingleton(getIdentityToken, defaultClient(getLocationAndBasename()))

    override fun getDispatcher(traceId: Uuid): CommandDispatcher = CommandDispatcher(
        traceId,
        if (window["inMemory"] == true) {
            MemoryRepositoryCatalog("test-user", backend, TimeProvider)
        } else {
            sdk
        }
    )
}

fun getLocationAndBasename(): Pair<String, String> {
    val location = window.location.origin
    val basename = "${window["basename"]}"
    return location to basename
}
