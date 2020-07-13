package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipped
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipper
import com.zegreatrob.coupling.client.pin.pinButton
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.css.Display
import kotlinx.css.display
import kotlinx.css.paddingBottom
import kotlinx.css.px
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RProps
import react.buildElement
import react.dom.*
import react.useState
import styled.css
import styled.styledDiv

external fun encodeURIComponent(input: String?)

data class PrepareSpinProps(
    val tribe: Tribe,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
    val pins: List<Pin>,
    val pathSetter: (String) -> Unit
) : RProps

private val styles = useStyles("PrepareSpin")

val PrepareSpin = reactFunction<PrepareSpinProps> { (tribe, players, history, pins, pathSetter) ->
    val (playerSelections, setPlayerSelections) = useState(players.map { it to isInLastSetOfPairs(it, history) })
    val (pinSelections, setPinSelections) = useState(pins.map { it._id })
    div(classes = styles.className) {
        div { tribeBrowser(tribe, pathSetter) }
        div {
            div { spinButton(tribe, playerSelections, pins.selectByIds(pinSelections), pathSetter) }
            optionalPinSelector(pins, pinSelections, setPinSelections)
            div(styles["player-selector"]) {
                h1 { +"Please select players to spin." }
                h2 { +"Tap a player to include or exclude them." }
                +"When you're done with your selections, hit the spin button above!"
                div {
                    selectAllButton(playerSelections, setPlayerSelections)
                    selectNoneButton(playerSelections, setPlayerSelections)
                }
                selectablePlayerCardList(playerSelections, tribe, pathSetter, setPlayerSelections)
            }
        }
    }
}

fun RBuilder.selectAllButton(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit
) = batchSelectButton(styles["selectAllButton"], "All in!", playerSelections, setPlayerSelections, true)

fun RBuilder.selectNoneButton(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit
) = batchSelectButton(styles["selectNoneButton"], "All out!", playerSelections, setPlayerSelections, false)

private fun RBuilder.batchSelectButton(
    className: String,
    text: String,
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit,
    selectionValue: Boolean
) = button(classes = "button") {
    attrs {
        classes += className
        onClickFunction = { playerSelections.map { it.copy(second = selectionValue) }.let(setPlayerSelections) }
    }
    +text
}


private fun RBuilder.optionalPinSelector(
    pins: List<Pin>,
    selectedPins: List<String?>,
    setPinSelections: (List<String?>) -> Unit
) {
    if (pins.isNotEmpty()) {
        child(pinSelector(selectedPins, setPinSelections, pins))
    }
}

private fun pinSelector(pinSelections: List<String?>, setPinSelections: (List<String?>) -> Unit, pins: List<Pin>) =
    buildElement {
        flipper(flipKey = pinSelections.generateFlipKey(), classes = styles["pinSelector"]) {
            div(classes = styles["selectedPins"]) {
                pins.selectByIds(pinSelections)
                    .map { pin ->
                        flippedPinButton(pin) { setPinSelections(pinSelections - pin._id) }
                    }
            }
            div(classes = styles["deselectedPins"]) {
                pins.removeByIds(pinSelections)
                    .map { pin ->
                        flippedPinButton(pin) { setPinSelections(pinSelections + pin._id) }
                    }
            }
        }
    }

private fun List<Pin>.selectByIds(pinSelections: List<String?>) = filter { pinSelections.contains(it._id) }

private fun List<Pin>.removeByIds(pinSelections: List<String?>) = filterNot { pinSelections.contains(it._id) }

private fun RBuilder.flippedPinButton(pin: Pin, onClick: () -> Unit = {}) = flipped(pin._id) {
    styledDiv {
        attrs { key = pin._id ?: "" }
        css { display = Display.inlineBlock }
        pinButton(pin, onClick = onClick, showTooltip = true)
    }
}

private fun List<String?>.generateFlipKey() = joinToString(",") { it ?: "null" }

private fun RBuilder.spinButton(
    tribe: Tribe,
    playerSelections: List<Pair<Player, Boolean>>,
    selectedPins: List<Pin>,
    pathSetter: (String) -> Unit
) = a(classes = "super pink button") {
    attrs {
        classes += styles["spinButton"]
        onClickFunction = { goToNewPairAssignments(pathSetter, tribe, playerSelections, selectedPins) }
    }
    +"Spin!"
}

private fun RBuilder.selectablePlayerCardList(
    playerSelections: List<Pair<Player, Boolean>>,
    tribe: Tribe,
    pathSetter: (String) -> Unit,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit
): List<Any> = playerSelections.map { (player, isSelected) ->
    styledDiv {
        css { paddingBottom = 30.px; display = Display.inlineBlock }
        playerCard(tribe, player, pathSetter, isSelected, setPlayerSelections, playerSelections)
    }
}

private fun RBuilder.playerCard(
    tribe: Tribe,
    player: Player,
    pathSetter: (String) -> Unit,
    isSelected: Boolean,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
    playerSelections: List<Pair<Player, Boolean>>
) = playerCard(PlayerCardProps(
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
    playerSelections: List<Pair<Player, Boolean>>,
    pinSelections: List<Pin>
) = pathSetter(
    "/${tribe.id.value}/pairAssignments/new?${buildQueryString(playerSelections, pinSelections)}"
)

private fun buildQueryString(playerSelections: List<Pair<Player, Boolean>>, pinSelections: List<Pin>) =
    (playerSelections.buildQueryParameters() + pinSelections.buildQueryParameters())
        .toQueryString()

private fun List<Pair<Player, Boolean>>.buildQueryParameters() = filter { (_, isSelected) -> isSelected }
    .map { it.first.id }.toProperty("player")


private fun List<Pin>.buildQueryParameters() = map { it._id }.toProperty("pin")

private fun List<Pair<String, String?>>.toQueryString() = toList().joinToString("&") { (propName, id) ->
    "$propName=${encodeURIComponent(id)}"
}

private fun List<String?>.toProperty(propName: String) = map { propName to it }

private fun isInLastSetOfPairs(player: Player, history: List<PairAssignmentDocument>) = if (history.isEmpty())
    false
else history.first()
    .pairs.map { it.players }
    .flatten()
    .map { it.player.id }
    .contains(player.id)
