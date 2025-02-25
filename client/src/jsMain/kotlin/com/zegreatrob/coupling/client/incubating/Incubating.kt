package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Color

val IncubatingPage by nfc<PageProps> { props ->
    CouplingQuery(
        commander = props.commander,
        query = graphQuery {
            partyList { details() }
            config {
                addToSlackUrl()
                discordClientId()
            }
        },
    ) { _, _, result ->
        val addToSlackUrl = result.config?.addToSlackUrl
        val discordClientId = result.config?.discordClientId
        if (addToSlackUrl != null && discordClientId != null) {
            IncubatingContent(
                discordClientId = discordClientId,
                addToSlackUrl = addToSlackUrl,
                partyList = result.partyList?.mapNotNull { it.details?.data } ?: emptyList(),
            )
        }
    }
}

external interface IncubatingContentProps : Props {
    var discordClientId: String
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
            AddToDiscordButton(props.partyList, props.discordClientId)
        }
    }
}
