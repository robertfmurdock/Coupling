package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.graphQuery
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
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.router.useHref
import react.useState
import web.cssom.Color

val IncubatingPage by nfc<PageProps> { props ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery { partyList(); addToSlackUrl() },
        toNode = { _, _, result ->
            result.addToSlackUrl?.let {
                IncubatingContent.create(it, result.partyList?.map(Record<PartyDetails>::data) ?: emptyList())
            }
        },
    )
}

external interface IncubatingContentProps : Props {
    var addToSlackUrl: String
    var partyList: List<PartyDetails>
}

@ReactFunc
val IncubatingContent by nfc<IncubatingContentProps> { props ->
    PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
        +"Incubating Features - Best not to touch"
        div {
            AddToSlackButton { url = props.addToSlackUrl }
        }
        div {
            AddToDiscordButton { partyList = props.partyList }
        }
    }
}

external interface AddToDiscordButtonProps : Props {
    var partyList: List<PartyDetails>
}

@ReactFunc
val AddToDiscordButton by nfc<AddToDiscordButtonProps> { props ->
    var showTools by useState(false)
    var selectedParty by useState<PartyId?>(null)
    val discordCallbackHref = useHref("/integration/discord/callback")

    if (!showTools) {
        CouplingButton(onClick = { showTools = true }) {
            +"Add to Discord"
        }
    } else {
        label {
            +"Select the party about which you'd like Discord messages."
            div {
                select {
                    onChange = {
                        selectedParty = it.currentTarget.value.let(::PartyId)
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
            a {
                href = URLBuilder("https://discord.com/api/oauth2/authorize").apply {
                    parameters.append("client_id", "1133538666661281862")
                    parameters.append("redirect_uri", "https://${window.location.host}$discordCallbackHref")
                    parameters.append("response_type", "code")
                    parameters.append("scope", "webhook.incoming")
                    parameters.append("state", Parameters.build { append("partyId", selectedParty?.value ?: "") }.formUrlEncode())
                }.toString()
                CouplingButton {
                    +"Onward to Discord!"
                }
            }
        }
    }
}
