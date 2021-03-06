package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths.newPairAssignmentsPath
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.pink
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipped
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipper
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsCommand
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pin.pinButton
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.*
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.animation
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.s
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.buildElement
import react.dom.*
import react.router.dom.redirect
import react.useState
import styled.css
import styled.styledDiv

data class PrepareSpinProps(
    val tribe: Tribe,
    val players: List<Player>,
    val currentPairsDoc: PairAssignmentDocument?,
    val pins: List<Pin>,
    val dispatchFunc: DispatchFunc<out NewPairAssignmentsCommandDispatcher>
) : RProps

private val styles = useStyles("PrepareSpin")

val PrepareSpin = reactFunction<PrepareSpinProps> { (tribe, players, currentPairsDoc, pins, dispatchFunc) ->
    val (playerSelections, setPlayerSelections) = useState(defaultSelections(players, currentPairsDoc))
    val (pinSelections, setPinSelections) = useState(pins.map { it.id })
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val onSpin = onSpin(dispatchFunc, tribe, playerSelections, pinSelections) { setRedirectUrl(it) }

    if (redirectUrl != null)
        redirect(to = redirectUrl)
    else
        div(classes = styles.className) {
            div { tribeBrowser(tribe) }
            div {
                div { spinButton(onSpin) }
                selectorAreaDiv {
                    playerSelectorDiv {
                        h1 { +"Please select players to spin." }
                        h2 { +"Tap a player to include or exclude them." }
                        +"When you're done with your selections, hit the spin button above!"
                        styledDiv {
                            css { margin(10.px, null) }
                            selectAllButton(playerSelections) { setPlayerSelections(it) }
                            selectNoneButton(playerSelections) { setPlayerSelections(it) }
                        }
                        selectablePlayerCardList(playerSelections, { setPlayerSelections(it) }, tribe)
                    }
                    if (pins.isNotEmpty()) {
                        pinSelectorDiv {
                            h1 { br {} }
                            h2 { +"Also, Pins." }
                            +"Tap any pin to skip."
                            child(pinSelector(pinSelections, { setPinSelections(it) }, pins))
                        }
                    }
                }
            }
        }
}

private fun onSpin(
    dispatchFunc: DispatchFunc<out NewPairAssignmentsCommandDispatcher>,
    tribe: Tribe,
    playerSelections: List<Pair<Player, Boolean>>,
    pinSelections: List<String?>,
    setRedirectUrl: (String) -> Unit
) = dispatchFunc(
    { NewPairAssignmentsCommand(tribe.id, playerSelections.playerIds(), pinSelections.filterNotNull()) },
    { setRedirectUrl(tribe.newPairAssignmentsPath()) }
)

private fun RBuilder.selectorAreaDiv(children: RBuilder.() -> Unit) = styledDiv {
    css {
        display = Display.flex
        borderSpacing = 5.px
        borderCollapse = BorderCollapse.separate
    }
    children()
}

private fun List<Pair<Player, Boolean>>.playerIds() = filter { (_, isSelected) -> isSelected }.map { it.first.id }

private fun RBuilder.playerSelectorDiv(children: RBuilder.() -> Unit) = styledDiv {
    css {
        display = Display.inlineBlock
        flex(1.0)
        margin(5.px)
        borderRadius = 20.px
        padding(5.px)

        backgroundColor = Color("#fffbed")
        boxShadow(rgba(0, 0, 0, 0.6), 1.px, 1.px, 3.px)
    }
    children()
}

private fun RBuilder.pinSelectorDiv(children: RBuilder.() -> Unit) = styledDiv {
    css {
        display = Display.inlineFlex
        flexDirection = FlexDirection.column
        margin(5.px)
        borderRadius = 20.px
        padding(5.px)
        backgroundColor = Color("#fffbed")
        boxShadow(rgba(0, 0, 0, 0.6), 1.px, 1.px, 3.px)
        width = 125.px
    }
    children()
}

private fun defaultSelections(players: List<Player>, currentPairsDoc: PairAssignmentDocument?) = players.map { player ->
    player to isInLastSetOfPairs(player, currentPairsDoc)
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
) = couplingButton(
    className = className,
    onClick = { playerSelections.map { it.copy(second = selectionValue) }.let(setPlayerSelections) },
) { +text }

private fun pinSelector(pinSelections: List<String?>, setPinSelections: (List<String?>) -> Unit, pins: List<Pin>) =
    buildElement {
        flipper(flipKey = pinSelections.generateFlipKey(), classes = styles["pinSelector"]) {
            selectedPinsDiv {
                pins.selectByIds(pinSelections)
                    .map { pin ->
                        flippedPinButton(pin) { setPinSelections(pinSelections - pin.id) }
                    }
            }
            deselectedPinsDiv {
                pins.removeByIds(pinSelections)
                    .map { pin ->
                        flippedPinButton(pin) { setPinSelections(pinSelections + pin.id) }
                    }
            }
        }
    }

private fun RBuilder.selectedPinsDiv(children: RBuilder.() -> Unit) = styledDiv {
    attrs { classes = classes + styles["selectedPins"] }
    css {
        margin(5.px)
        flex(1.0)
    }
    children()
}

private fun RBuilder.deselectedPinsDiv(children: RBuilder.() -> Unit) = styledDiv {
    attrs { classes = classes + styles["deselectedPins"] }
    css {
        flex(1.0)
        margin(5.px)
        backgroundColor = Color("#de8286")
        borderRadius = 15.px
    }
    children()
}

private fun List<Pin>.selectByIds(pinSelections: List<String?>) = filter { pinSelections.contains(it.id) }

private fun List<Pin>.removeByIds(pinSelections: List<String?>) = filterNot { pinSelections.contains(it.id) }

private fun RBuilder.flippedPinButton(pin: Pin, onClick: () -> Unit = {}) = flipped(pin.id) {
    styledDiv {
        attrs { key = pin.id ?: "" }
        css { display = Display.inlineBlock }
        pinButton(pin, onClick = onClick, showTooltip = true)
    }
}

private fun List<String?>.generateFlipKey() = joinToString(",") { it ?: "null" }

private fun RBuilder.spinButton(generateNewPairsFunc: () -> Unit) = couplingButton(
    supersize,
    pink,
    styles["spinButton"],
    onClick = generateNewPairsFunc,
    block = {
        css {
            marginBottom = 10.px
            animation("pulsate", 2.s, iterationCount = IterationCount.infinite)
        }
    }
) {
    +"Spin!"
}

private fun RBuilder.selectablePlayerCardList(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
    tribe: Tribe
) = playerSelections.map { (player, isSelected) ->
    styledDiv {
        css { paddingBottom = 30.px; display = Display.inlineBlock }
        playerCard(tribe, player, isSelected, setPlayerSelections, playerSelections)
    }
}

private fun RBuilder.playerCard(
    tribe: Tribe,
    player: Player,
    isSelected: Boolean,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
    playerSelections: List<Pair<Player, Boolean>>
) = playerCard(PlayerCardProps(
    tribe.id,
    player,
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


private fun isInLastSetOfPairs(player: Player, currentPairsDoc: PairAssignmentDocument?) = currentPairsDoc
    ?.pairs
    ?.map { it.players }
    ?.flatten()
    ?.map { it.player.id }
    ?.contains(player.id)
    ?: false
