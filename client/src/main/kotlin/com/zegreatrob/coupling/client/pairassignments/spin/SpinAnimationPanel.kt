package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipped
import com.zegreatrob.coupling.client.pairassignments.AssignedPair
import com.zegreatrob.coupling.client.pairassignments.PairAssignmentsHeader
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.Display
import kotlinx.css.Visibility
import kotlinx.css.display
import kotlinx.css.visibility
import react.RBuilder
import react.dom.attrs
import react.dom.div
import react.dom.key
import styled.css
import styled.styledDiv

data class SpinAnimationPanel(
    val tribe: Tribe,
    val rosteredPairAssignments: RosteredPairAssignments,
    val state: SpinAnimationState
) : DataProps<SpinAnimationPanel> {
    override val component: TMFC<SpinAnimationPanel> get() = spinAnimationPanel
}

val placeholderPlayer = Player("?", name = "Next...", callSignAdjective = "--------", callSignNoun = "--------")

data class SpinStateData(
    val rosterPlayers: List<Player>,
    val revealedPairs: List<PinnedCouplingPair>,
    val shownPlayer: Player?
)

private val styles = useStyles("pairassignments/SpinAnimation")

fun RBuilder.spinAnimation(tribe: Tribe, rosteredPairAssignments: RosteredPairAssignments, state: SpinAnimationState) =
    child(SpinAnimationPanel(tribe, rosteredPairAssignments, state))

val spinAnimationPanel = reactFunction<SpinAnimationPanel> { (tribe, rosteredPairAssignments, state) ->
    val pairAssignments = rosteredPairAssignments.pairAssignments
    val players = rosteredPairAssignments.selectedPlayers
    val (rosterPlayers, revealedPairs, shownPlayer) = state.stateData(players, pairAssignments)
    div {
        child(
            PairAssignmentsHeader(pairAssignments)
        )
        assignedPairs(tribe, revealedPairs)
        playerSpotlight(shownPlayer)
        playerRoster(rosterPlayers)
    }
}

private fun RBuilder.assignedPairs(tribe: Tribe, revealedPairs: List<PinnedCouplingPair>) = div(
    classes = styles["pairAssignments"]
) {
    revealedPairs.mapIndexed { index, it -> child(AssignedPair(tribe, it, false), key = "$index") }
}

private fun RBuilder.playerSpotlight(shownPlayer: Player?) = div(classes = styles["playerSpotlight"]) {
    if (shownPlayer != null)
        flippedPlayer(shownPlayer)
    else
        placeholderPlayerCard()
}

private fun RBuilder.placeholderPlayerCard() = styledDiv {
    css { visibility = Visibility.hidden; display = Display.inlineBlock }
    flippedPlayer(placeholderPlayer)
}

private fun RBuilder.flippedPlayer(player: Player, key: String? = null) = flipped(player.id) {
    styledDiv {
        attrs { this.key = key ?: "" }
        css { display = Display.inlineBlock }
        child(PlayerCard(TribeId(""), player))
    }
}

private fun RBuilder.playerRoster(players: List<Player>) = div(classes = styles["playerRoster"]) {
    players.map {
        if (it == placeholderPlayer) {
            placeholderPlayerCard()
        } else {
            flippedPlayer(it, key = it.id)
        }
    }
}
