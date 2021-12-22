package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipped
import com.zegreatrob.coupling.client.pairassignments.AssignedPair
import com.zegreatrob.coupling.client.pairassignments.PairAssignmentsHeader
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.Display
import kotlinx.css.Visibility
import kotlinx.css.display
import kotlinx.css.visibility
import react.buildElement
import react.create
import react.dom.attrs
import react.dom.html.ReactHTML.div
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

val spinAnimationPanel = tmFC<SpinAnimationPanel> { (tribe, rosteredPairAssignments, state) ->
    val pairAssignments = rosteredPairAssignments.pairAssignments
    val players = rosteredPairAssignments.selectedPlayers
    val (rosterPlayers, revealedPairs, shownPlayer) = state.stateData(players, pairAssignments)
    div {
        child(PairAssignmentsHeader(pairAssignments))
        +assignedPairs(tribe, revealedPairs)
        +playerSpotlight(shownPlayer)
        +playerRoster(rosterPlayers)
    }
}

private fun assignedPairs(tribe: Tribe, revealedPairs: List<PinnedCouplingPair>) = div.create {
    className = styles["pairAssignments"]
    revealedPairs.forEachIndexed { index, it -> child(AssignedPair(tribe, it, false), key = "$index") }
}

private fun playerSpotlight(shownPlayer: Player?) = div.create {
    className = styles["playerSpotlight"]
    if (shownPlayer != null)
        +flippedPlayer(shownPlayer)
    else
        +placeholderPlayerCard()
}

private fun placeholderPlayerCard() = buildElement {
    styledDiv {
        css { visibility = Visibility.hidden; display = Display.inlineBlock }
        +flippedPlayer(placeholderPlayer)
    }
}

private fun flippedPlayer(player: Player, key: String? = null) = buildElement {
    flipped(player.id) {
        styledDiv {
            attrs { this.key = key ?: "" }
            css { display = Display.inlineBlock }
            child(PlayerCard(TribeId(""), player))
        }
    }
}

private fun playerRoster(players: List<Player>) = div.create {
    className = styles["playerRoster"]
    players.forEach { player ->
        if (player == placeholderPlayer) {
            +placeholderPlayerCard()
        } else {
            +flippedPlayer(player, key = player.id)
        }
    }
}
