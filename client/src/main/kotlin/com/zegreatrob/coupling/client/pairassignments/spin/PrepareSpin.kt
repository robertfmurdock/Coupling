package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pin.PinButton.pinButton
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.tribe.TribeBrowserProps
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.a
import react.dom.div

external fun encodeURIComponent(input: String?)

data class PrepareSpinProps(
    val tribe: Tribe,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
    val pins: List<Pin>,
    val pathSetter: (String) -> Unit
) : RProps

object PrepareSpin : RComponent<PrepareSpinProps>(provider()), PrepareSpinRenderer

interface PrepareSpinRenderer : StyledComponentRenderer<PrepareSpinProps, SimpleStyle> {

    override val componentPath: String get() = "PrepareSpin"

    override fun StyledRContext<PrepareSpinProps, SimpleStyle>.render(): ReactElement {
        val (tribe, players, history, pins, pathSetter) = props
        val (playerSelections, setPlayerSelections) = useState(
            players.map { it to isInLastSetOfPairs(it, history) }
        )
        val (pinSelections, setPinSelections) = useState(pins)
        return reactElement {
            div(classes = styles.className) {
                div { tribeBrowser(TribeBrowserProps(tribe, pathSetter)) }
                div {
                    div { spinButton(tribe, playerSelections, pathSetter, styles["spinButton"]) }
                    optionalPinSelector(pins, pinSelections, setPinSelections, styles)
                    selectablePlayerCardList(playerSelections, tribe, pathSetter, setPlayerSelections, styles)
                }
            }
        }
    }

    private fun RBuilder.optionalPinSelector(
        pins: List<Pin>,
        selectedPins: List<Pin>,
        setPinSelections: (List<Pin>) -> Unit,
        styles: SimpleStyle
    ) {
        if (pins.isNotEmpty()) {
            div(classes = styles["pinSelector"]) {
                div(classes = styles["selectedPins"]) {
                    selectedPins.map { pin ->
                        pinButton(pin, onClick = { setPinSelections(selectedPins - pin) }, key = pin._id)
                    }
                }
                div(classes = styles["deselectedPins"]) {
                    (pins - selectedPins)
                        .map { pin -> pinButton(pin, key = pin._id) }
                }
            }
        }
    }

    private fun RBuilder.spinButton(
        tribe: Tribe,
        playerSelections: List<Pair<Player, Boolean>>,
        pathSetter: (String) -> Unit,
        className: String
    ) = a(classes = "super pink button") {
        attrs {
            classes += className
            onClickFunction = { goToNewPairAssignments(pathSetter, tribe, playerSelections) }
        }
        +"Spin!"
    }

    private fun RBuilder.selectablePlayerCardList(
        playerSelections: List<Pair<Player, Boolean>>,
        tribe: Tribe,
        pathSetter: (String) -> Unit,
        setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
        styles: SimpleStyle
    ) = playerSelections.map { (player, isSelected) ->
        playerCard(tribe, player, pathSetter, isSelected, setPlayerSelections, playerSelections, styles)
    }

    private fun RBuilder.playerCard(
        tribe: Tribe,
        player: Player,
        pathSetter: (String) -> Unit,
        isSelected: Boolean,
        setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
        playerSelections: List<Pair<Player, Boolean>>,
        styles: SimpleStyle
    ) {
        playerCard(PlayerCardProps(
            tribe.id,
            player,
            pathSetter,
            true,
            className = styles["playerCard"],
            deselected = !isSelected,
            onClick = {
                setPlayerSelections(
                    flipSelectionForPlayer(player, isSelected, playerSelections)
                )
            }
        ))
    }

    private fun flipSelectionForPlayer(
        targetPlayer: Player,
        targetIsSelected: Boolean,
        playerSelections: List<Pair<Player, Boolean>>
    ) = playerSelections.map { pair ->
        if (pair.first == targetPlayer) {
            Pair(targetPlayer, !targetIsSelected)
        } else {
            pair
        }
    }

    private fun goToNewPairAssignments(
        pathSetter: (String) -> Unit,
        tribe: Tribe,
        playerSelections: List<Pair<Player, Boolean>>
    ) = pathSetter(
        "/${tribe.id.value}/pairAssignments/new?${playerSelections.buildQueryParameters()}"
    )

    private fun List<Pair<Player, Boolean>>.buildQueryParameters() = filter { (_, isSelected) -> isSelected }
        .joinToString("&") { (player, _) ->
            "player=${encodeURIComponent(player.id)}"
        }

    private fun isInLastSetOfPairs(player: Player, history: List<PairAssignmentDocument>) = if (history.isEmpty()) {
        true
    } else {
        history.first()
            .pairs.map { it.players }
            .flatten()
            .map { it.player.id }
            .contains(player.id)
    }
}
