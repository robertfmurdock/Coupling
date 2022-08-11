package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.pairassignments.AssignedPair
import com.zegreatrob.coupling.client.pairassignments.PairAssignmentsHeader
import com.zegreatrob.coupling.components.PlayerCard
import com.zegreatrob.coupling.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.components.spin.RosteredPairAssignments
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Display
import csstype.Position
import csstype.Visibility
import csstype.integer
import csstype.pct
import csstype.translate
import emotion.css.ClassName
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div

data class SpinAnimationPanel(
    val party: Party,
    val rosteredPairAssignments: RosteredPairAssignments,
    val state: SpinAnimationState
) : DataPropsBind<SpinAnimationPanel>(spinAnimationPanel)

val placeholderPlayer = Player("?", name = "Next...", callSignAdjective = "--------", callSignNoun = "--------")

data class SpinStateData(
    val rosterPlayers: List<Player>,
    val revealedPairs: List<PinnedCouplingPair>,
    val shownPlayer: Player?
)

val spinAnimationPanel = tmFC<SpinAnimationPanel> { (party, rosteredPairAssignments, state) ->
    val pairAssignments = rosteredPairAssignments.pairAssignments
    val players = rosteredPairAssignments.selectedPlayers
    val (rosterPlayers, revealedPairs, shownPlayer) = state.stateData(players, pairAssignments)
    div {
        add(PairAssignmentsHeader(pairAssignments))
        assignedPairs(party, revealedPairs)
        playerSpotlight(shownPlayer)
        playerRoster(rosterPlayers)
    }
}

val pairAssignmentStyles = ClassName {
}

private fun ChildrenBuilder.assignedPairs(party: Party, revealedPairs: List<PinnedCouplingPair>) = div {
    className = pairAssignmentStyles
    revealedPairs.forEachIndexed { index, it -> add(AssignedPair(party, it, false)) { key = "$index" } }
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

    if (shownPlayer != null)
        flippedPlayer(shownPlayer)
    else
        placeholderPlayerCard()
}

private fun ChildrenBuilder.placeholderPlayerCard() =
    div {
        css { visibility = Visibility.hidden; display = Display.inlineBlock }
        flippedPlayer(placeholderPlayer)
    }

private fun ChildrenBuilder.flippedPlayer(player: Player, key: String? = null) = Flipped {
    flipId = player.id
    div {
        this.key = key ?: ""
        css { display = Display.inlineBlock }
        add(PlayerCard(player))
    }
}
val playerRosterStyles = ClassName {
}

private fun ChildrenBuilder.playerRoster(players: List<Player>) = div {
    className = playerRosterStyles
    players.forEach { player ->
        if (player == placeholderPlayer) {
            placeholderPlayerCard()
        } else {
            flippedPlayer(player, key = player.id)
        }
    }
}
