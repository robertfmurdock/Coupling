package com.zegreatrob.coupling.client.components.contribution

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

external interface ContributionContentFrameProps : PropsWithChildren {
    var party: PartyDetails
}

val contributionContentBackgroundColor = Color("rgb(253 237 189)")

@ReactFunc
val ContributionContentFrame by nfc<ContributionContentFrameProps> { props ->
    val partyId = props.party.id
    div {
        PageFrame(borderColor = Color("rgb(255 143 117)"), backgroundColor = contributionContentBackgroundColor) {
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
