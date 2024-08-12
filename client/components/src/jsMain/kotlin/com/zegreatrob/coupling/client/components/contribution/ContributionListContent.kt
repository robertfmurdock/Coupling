package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.stats.ContributionControlPanelFrame
import com.zegreatrob.coupling.client.components.stats.ContributionLabelFilter
import com.zegreatrob.coupling.client.components.stats.EnumSelector
import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import web.cssom.Display
import web.cssom.em

external interface ContributionListContentProps : Props {
    var party: PartyDetails
    var contributions: List<Contribution>
    var contributors: List<Player>
    var window: GqlContributionWindow
    var setWindow: (GqlContributionWindow) -> Unit
}

@ReactFunc
val ContributionListContent by nfc<ContributionListContentProps> { (_, contributions, contributors, window, setWindow) ->
    div {
        div {
            css { display = Display.inlineBlock }
            h2 {
                +"Contributions for the last $window:"
            }
            div {
                ContributionControlPanelFrame {
                    div {
                        EnumSelector(
                            default = window,
                            onChange = setWindow,
                            label = ReactNode("Time Window"),
                            backgroundColor = contributionContentBackgroundColor,
                        )
                    }
                    div {
                        ContributionLabelFilter(
                            allLabels = emptySet(),
                            selected = null,
                            setSelected = {},
                        )
                    }
                }
            }
            div {
                css { fontSize = 0.75.em }
                contributions.forEach { contribution ->
                    ContributionCard(contribution = contribution, contributors = contributors, key = contribution.id)
                }
            }
        }
    }
}
