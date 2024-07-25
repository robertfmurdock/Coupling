package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.Editor
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.formUrlEncode
import kotlinx.browser.window
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.router.useHref
import react.useState

external interface AddToDiscordButtonProps : Props {
    var partyList: List<PartyDetails>
    var discordClientId: String
}

@ReactFunc
val AddToDiscordButton by nfc<AddToDiscordButtonProps> { props ->
    var showTools by useState(false)
    var selectedParty by useState<PartyId?>(null)
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
                                    key = it.id.value
                                    value = it.id.value
                                    +it.name
                                }
                            }
                        }
                    }
                }
                if (selectedParty != null) {
                    a {
                        href = URLBuilder("https://discord.com/api/oauth2/authorize").apply {
                            parameters.append("client_id", props.discordClientId)
                            parameters.append("redirect_uri", "https://${window.location.host}$discordCallbackHref")
                            parameters.append("response_type", "code")
                            parameters.append("scope", "webhook.incoming")
                            parameters.append(
                                "state",
                                Parameters.build { append("partyId", selectedParty?.value ?: "") }.formUrlEncode(),
                            )
                        }.toString()
                        CouplingButton {
                            +"Onward to Discord!"
                        }
                    }
                }
            }
        }
    }
}
