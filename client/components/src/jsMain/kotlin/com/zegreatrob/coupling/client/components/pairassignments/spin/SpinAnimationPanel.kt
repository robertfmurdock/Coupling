package com.zegreatrob.coupling.client.components.pairassignments.spin

import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.components.pairassignments.AssignedPair
import com.zegreatrob.coupling.client.components.pairassignments.PairAssignmentsHeader
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.spin.RosteredPairAssignments
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import react.ChildrenBuilder
import react.Fragment
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Display
import web.cssom.Position
import web.cssom.Visibility
import web.cssom.integer
import web.cssom.pct
import web.cssom.translate

external interface SpinAnimationPanelProps : Props {
    var party: PartyDetails
    var rosteredPairAssignments: RosteredPairAssignments
    var state: SpinAnimationState
}

@ReactFunc
val SpinAnimationPanel by nfc<SpinAnimationPanelProps> { (party, rosteredPairAssignments, state) ->
    val pairAssignments = rosteredPairAssignments.pairAssignments
    val players = rosteredPairAssignments.selectedPlayers
    val (rosterPlayers, revealedPairs, shownPlayer) = state.stateData(players, pairAssignments)
    div {
        PairAssignmentsHeader(pairAssignments)
        assignedPairs(party, revealedPairs)
        playerSpotlight(shownPlayer)
        playerRoster(rosterPlayers)
    }
}

private fun ChildrenBuilder.assignedPairs(party: PartyDetails, revealedPairs: List<PinnedCouplingPair>) = div {
    asDynamic()["data-testid"] = "assigned-pairs"
    revealedPairs.forEachIndexed { index, it -> AssignedPair(party, it, false, key = "$index") }
}

val playerSpotlightStyles = ClassName {
    position = Position.relative
    "> div" {
        position = Position.absolute
        zIndex = integer(1)
        transform = translate((-50).pct, (-50).pct)
    }
}

private fun ChildrenBuilder.playerSpotlight(shownPlayer: Player?) = div {
    className = playerSpotlightStyles

    if (shownPlayer != null) {
        flippedPlayer(shownPlayer)
    } else {
        placeholderPlayerCard()
    }
}

private fun ChildrenBuilder.placeholderPlayerCard() = div {
    css {
        visibility = Visibility.hidden
        display = Display.inlineBlock
    }
    flippedPlayer(placeholderPlayer)
}

private fun ChildrenBuilder.flippedPlayer(player: Player, key: String? = null) = Flipped {
    flipId = player.id.value.toString()
    this.key = key ?: ""
    div {
        css { display = Display.inlineBlock }
        PlayerCard(player)
    }
}

private fun ChildrenBuilder.playerRoster(players: List<Player>) = div {
    asDynamic()["data-testid"] = "player-roster"
    players.forEach { player ->
        Fragment {
            key = player.id.value.toString()
            if (player.id.value.toString().startsWith("?")) {
                placeholderPlayerCard()
            } else {
                flippedPlayer(player)
            }
        }
    }
}
