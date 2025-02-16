package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.ClientDispatcher
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.couplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.testmints.action.DispatcherPipeCannon
import js.objects.ReadonlyRecord
import kotlinx.browser.window
import org.w3c.dom.get
import react.Props
import web.url.URLSearchParams
import kotlin.uuid.Uuid

external interface PageProps : Props {
    var pathParams: ReadonlyRecord<String, String>
    var commander: Commander
    var search: URLSearchParams
    var config: ClientConfig
}

val PageProps.partyId: PartyId? get() = pathParams["partyId"]?.let(::PartyId)
val PageProps.playerId: String? get() = pathParams["playerId"]
val PageProps.pinId: String? get() = pathParams["pinId"]

interface Commander {
    fun tracingCannon(traceId: Uuid): DispatcherPipeCannon<CouplingSdkDispatcher>
    fun tracingCannon() = tracingCannon(Uuid.random())
        .let { DispatcherPipeCannon(ClientDispatcher(it.dispatcher), it.pipe) }
}

class MasterCommander(private val getIdentityToken: suspend () -> String) : Commander {
    override fun tracingCannon(traceId: Uuid) = couplingSdk(
        getIdTokenFunc = getIdentityToken,
        httpClient = defaultClient(apiUrl(), traceId),
        pipe = LoggingActionPipe(traceId),
    )
}

fun apiUrl(): String = "https://${window.location.hostname}${window["basename"]}/"
