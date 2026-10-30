package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.client.discord.DiscordOAuthFlow
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import io.ktor.http.URLBuilder
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.Key
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.router.useHref
import react.useState
import kotlin.uuid.Uuid

external interface AddToDiscordButtonProps : Props {
    var partyList: List<PartyDetails>
    var commander: Commander
    var discordClientId: String
}

@ReactFunc
val AddToDiscordButton by nfc<AddToDiscordButtonProps> { props ->
    var showTools by useState(false)
    var selectedParty by useState<PartyId?>(null)
    var pending by useState(false)
    var error by useState<String?>(null)
    val discordCallbackHref = useHref("/integration/discord/callback")

    if (!showTools) {
        CouplingButton {
            onClick = { showTools = true }
            +"Add to Discord"
        }
    } else {
        Editor {
            label {
                +"Select the party about which you'd like Discord messages."
                div {
                    li {
                        select {
                            onChange = {
                                selectedParty = it.currentTarget.value.let(::PartyId)
                            }
                            if (selectedParty == null) {
                                option {
                                    +"Please select a party"
                                }
                            }
                            props.partyList.forEach {
                                option {
                                    key = Key(it.id.value.toString())
                                    value = it.id.value
                                    +it.name
                                }
                            }
                        }
                    }
                }
                if (selectedParty != null) {
                    CouplingButton {
                        onClick = {
                            val partyId = selectedParty
                            if (partyId != null && !pending) {
                                pending = true
                                error = null
                                MainScope().launch {
                                    try {
                                        val state = DiscordOAuthFlow(props.commander.tracingCannon(Uuid.random())).begin(partyId)
                                        window.location.href = discordAuthorizationUrl(props.discordClientId, discordCallbackHref, state)
                                    } catch (_: Exception) {
                                        error = "Unable to start Discord installation. Please try again."
                                        pending = false
                                    }
                                }
                            }
                        }
                        +if (pending) "Starting Discord installation…" else "Onward to Discord!"
                    }
                    error?.let { message -> div { +message } }
                }
            }
        }
    }
}

private fun discordAuthorizationUrl(clientId: String, callbackHref: String, state: String): String = URLBuilder("https://discord.com/api/oauth2/authorize").apply {
    parameters.append("client_id", clientId)
    parameters.append("redirect_uri", "https://${window.location.host}$callbackHref")
    parameters.append("response_type", "code")
    parameters.append("scope", "webhook.incoming")
    parameters.append("state", state)
}.toString()
