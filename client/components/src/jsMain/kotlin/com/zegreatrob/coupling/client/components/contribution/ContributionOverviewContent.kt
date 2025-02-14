package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.ContributionPopUpMenu
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import popper.core.ReferenceElement
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.FlexDirection
import web.html.HTMLElement

external interface ContributionOverviewContentProps : Props {
    var party: PartyDetails
    var contributions: List<Contribution>
    var contributors: List<Player>
    var players: List<Player>
    var dispatchFunc: DispatchFunc<SavePlayerCommand.Dispatcher>
}

@ReactFunc
val ContributionOverviewContent by nfc<ContributionOverviewContentProps> { props ->
    val (party, contributions, contributors, players) = props
    div {
        div {
            css {
                display = Display.inlineFlex
                flexDirection = FlexDirection.column
                alignItems = AlignItems.center
            }
            h2 { +"Most Recent ${contributions.size} Contributions:" }
            ContributionPopUpMenu(party.id, players, props.dispatchFunc, child = { setSelectedPlayerCard ->
                val onPlayerClick = { player: Player, element: HTMLElement ->
                    setSelectedPlayerCard(ReferenceElement(element)!!, player)
                }
                Fragment.create {
                    contributions.forEach { contribution ->
                        ContributionCard(
                            key = contribution.id,
                            contribution = contribution,
                            contributors = contributors,
                            onPlayerClick = onPlayerClick,
                        )
                    }
                }
            })
        }
    }
}
