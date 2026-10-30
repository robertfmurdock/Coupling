package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.routing.Commander
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML
import web.cssom.Color

external interface IncubatingContentProps : Props {
    var commander: Commander
    var discordClientId: String
    var addToSlackUrl: String
    var partyList: List<PartyDetails>
}

@ReactFunc
val IncubatingContent by nfc<IncubatingContentProps> { props ->
    PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
        +"Incubating Features - Best not to touch"
        ReactHTML.div {
            AddToSlackButton { url = props.addToSlackUrl }
        }
        ReactHTML.div {
            AddToDiscordButton {
                partyList = props.partyList
                discordClientId = props.discordClientId
                commander = props.commander
            }
        }
    }
}
