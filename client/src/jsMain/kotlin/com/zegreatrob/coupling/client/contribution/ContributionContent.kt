package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.ContributionOverviewButton
import com.zegreatrob.coupling.client.components.ContributionVisualizationButton
import com.zegreatrob.coupling.client.components.NavigationPanel
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import web.cssom.Color

external interface ContributionContentProps : PropsWithChildren {
    var party: PartyDetails
}

@ReactFunc
val ContributionContent by nfc<ContributionContentProps> { props ->
    val partyId = props.party.id
    div {
        PageFrame(borderColor = Color("rgb(255 143 117)"), backgroundColor = Color("rgb(253 237 189)")) {
            ConfigHeader {
                party = props.party
                +"Contributions"
            }
            NavigationPanel {
                ContributionOverviewButton(partyId)
                ContributionVisualizationButton(partyId)
            }
            +props.children
        }
    }
}
