package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.pairassignments.AssignedPair
import com.zegreatrob.coupling.client.pairassignments.PairAssignmentsHeader
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.Display
import kotlinx.css.Visibility
import kotlinx.css.display
import kotlinx.css.visibility
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.key

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

private val styles = useStyles("pairassignments/SpinAnimation")

val spinAnimationPanel = tmFC<SpinAnimationPanel> { (party, rosteredPairAssignments, state) ->
    val pairAssignments = rosteredPairAssignments.pairAssignments
    val players = rosteredPairAssignments.selectedPlayers
    val (rosterPlayers, revealedPairs, shownPlayer) = state.stateData(players, pairAssignments)
    div {
        child(PairAssignmentsHeader(pairAssignments))
        assignedPairs(party, revealedPairs)
        playerSpotlight(shownPlayer)
        playerRoster(rosterPlayers)
    }
}

private fun ChildrenBuilder.assignedPairs(party: Party, revealedPairs: List<PinnedCouplingPair>) = div {
    className = styles["pairAssignments"]
    revealedPairs.forEachIndexed { index, it -> child(AssignedPair(party, it, false), key = "$index") }
}

private fun ChildrenBuilder.playerSpotlight(shownPlayer: Player?) = div {
    className = styles["playerSpotlight"]
    if (shownPlayer != null)
        flippedPlayer(shownPlayer)
    else
        placeholderPlayerCard()
}

private fun ChildrenBuilder.placeholderPlayerCard() =
    cssDiv(css = { visibility = Visibility.hidden; display = Display.inlineBlock }) {
        flippedPlayer(placeholderPlayer)
    }


private fun ChildrenBuilder.flippedPlayer(player: Player, key: String? = null) = Flipped {
    flipId = player.id
    cssDiv(props = { this.key = key ?: "" }, css = { display = Display.inlineBlock }) {
        child(PlayerCard(player))
    }
}

private fun ChildrenBuilder.playerRoster(players: List<Player>) = div {
    className = styles["playerRoster"]
    players.forEach { player ->
        if (player == placeholderPlayer) {
            placeholderPlayerCard()
        } else {
            flippedPlayer(player, key = player.id)
        }
    }
}
