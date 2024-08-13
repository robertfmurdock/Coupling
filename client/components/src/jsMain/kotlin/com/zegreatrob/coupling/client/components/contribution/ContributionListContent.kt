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
import react.useState
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
    val allLabels = contributions.mapNotNull(Contribution::label).toSet()
    val (selectedLabelFilter, setSelectedLabelFilter) = useState<String?>(null)
    val filteredContributions = selectedLabelFilter?.let { contributions.filter { it.label == selectedLabelFilter } }
        ?: contributions
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
                            allLabels = allLabels,
                            selected = selectedLabelFilter,
                            setSelected = setSelectedLabelFilter::invoke,
                        )
                    }
                }
            }
            div {
                css { fontSize = 0.75.em }
                filteredContributions.forEach { contribution ->
                    ContributionCard(contribution = contribution, contributors = contributors, key = contribution.id)
                }
            }
        }
    }
}
