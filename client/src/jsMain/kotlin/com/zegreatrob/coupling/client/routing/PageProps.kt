package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.ClientDispatcher
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.couplingSdk
import com.zegreatrob.coupling.sdk.defaultClient
import com.zegreatrob.testmints.action.DispatcherPipeCannon
import js.objects.ReadonlyRecord
import kotlinx.browser.window
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import org.w3c.dom.get
import react.Props
import tanstack.router.core.ParamName
import kotlin.uuid.Uuid

external interface PageProps : Props {
    var pathParams: ReadonlyRecord<ParamName, String>
    var commander: Commander
    var search: ReadonlyRecord<String, Any?>
    var config: ClientConfig
}

@OptIn(ExperimentalKotoolsTypesApi::class)
val PageProps.partyId: PartyId? get() = pathParams[ParamName("partyId")]?.let(::PartyId)
val PageProps.playerId: PlayerId? get() = pathParams[ParamName("playerId")]?.toNotBlankString()?.getOrNull()?.let(::PlayerId)
val PageProps.pinId: PinId? get() = pathParams[ParamName("pinId")]?.toNotBlankString()?.getOrNull()?.let(::PinId)

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

fun apiUrl(): String = "https://${window.location.hostname}${window["basename"]}"
