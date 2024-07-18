package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Display

external interface ContributionOverviewContentProps : Props {
    var party: PartyDetails
    var contributions: List<Contribution>
}

@ReactFunc
val ContributionOverviewContent by nfc<ContributionOverviewContentProps> { (_, contributions) ->
    div {
        div {
            css { display = Display.inlineBlock }
            contributions.forEach { contribution ->
                ContributionCard(contribution, key = contribution.id)
            }
        }
    }
}
