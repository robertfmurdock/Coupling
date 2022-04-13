package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.PageFrame
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.cssSpan
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.pink
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.pin.PinButton
import com.zegreatrob.coupling.client.pin.PinButtonScale
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.tribe.TribeBrowser
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import kotlinx.css.*
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.animation
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.s
import kotlinx.html.classes
import react.ChildrenBuilder
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.key

private val styles = useStyles("PrepareSpin")

data class PrepareSpinContent(
    var tribe: Party,
    var playerSelections: List<Pair<Player, Boolean>>,
    var pins: List<Pin>,
    var pinSelections: List<String?>,
    var setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit,
    var setPinSelections: (List<String?>) -> Unit,
    var onSpin: () -> Unit
) : DataPropsBind<PrepareSpinContent>(prepareSpinContent)

val prepareSpinContent = tmFC<PrepareSpinContent> { props ->
    val (tribe, playerSelections, pins, pinSelections, setPlayerSelections, setPinSelections, onSpin) = props

    val enabled = playerSelections.any { it.second }

    div {
        className = styles.className
        child(PageFrame(Color("#ff8c00"), backgroundColor = Color("#faf0d2"))) {
            div { child(TribeBrowser(tribe)) }
            div {
                div {
                    spinButton(onSpin, enabled = enabled)
                    if (!enabled) {
                        cssSpan(css = {
                            position = Position.absolute
                            width = 12.em
                        }) { +"Please tap a player to include them before spinning." }
                    }
                }
                selectorAreaDiv {
                    playerSelectorDiv {
                        h1 { +"Please select players to spin." }
                        h2 { +"Tap a player to include or exclude them." }
                        +"When you're done with your selections, hit the spin button above!"
                        cssDiv(css = { margin(10.px, null) }) {
                            selectAllButton(playerSelections, setPlayerSelections)
                            selectNoneButton(playerSelections, setPlayerSelections)
                        }
                        selectablePlayerCardList(playerSelections, setPlayerSelections)
                    }
                    if (pins.isNotEmpty()) {
                        pinSelectorDiv {
                            h1 { br {} }
                            h2 { +"Also, Pins." }
                            +"Tap any pin to skip."
                            pinSelector(pinSelections, setPinSelections, pins)
                        }
                    }
                }
            }
        }
    }
}

private fun ChildrenBuilder.selectorAreaDiv(children: ChildrenBuilder.() -> Unit) = cssDiv(
    css = {
        display = Display.flex
        borderSpacing = 5.px
        borderCollapse = BorderCollapse.separate
    }
) {
    children()
}

private fun ChildrenBuilder.playerSelectorDiv(children: ChildrenBuilder.() -> Unit) = cssDiv(
    css = {
        classes.add("${styles["playerSelector"]}")
        display = Display.inlineBlock
        flex(1.0)
        margin(5.px)
        borderRadius = 20.px
        padding(5.px)
        backgroundColor = Color("#fffbed")
        boxShadow(rgba(0, 0, 0, 0.6), 1.px, 1.px, 3.px)
    }
) { children() }

private fun ChildrenBuilder.pinSelectorDiv(children: ChildrenBuilder.() -> Unit) = cssDiv(
    css = {
        display = Display.inlineFlex
        flexDirection = FlexDirection.column
        margin(5.px)
        borderRadius = 20.px
        padding(5.px)
        backgroundColor = Color("#fffbed")
        boxShadow(rgba(0, 0, 0, 0.6), 1.px, 1.px, 3.px)
        width = 125.px
    }
) {
    children()
}

private fun ChildrenBuilder.selectAllButton(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit
) = batchSelectButton(styles["selectAllButton"], "All in!", playerSelections, setPlayerSelections, true)

private fun ChildrenBuilder.selectNoneButton(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit
) = batchSelectButton(styles["selectNoneButton"], "All out!", playerSelections, setPlayerSelections, false)

private fun ChildrenBuilder.batchSelectButton(
    className: ClassName,
    text: String,
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit,
    selectionValue: Boolean
) = child(CouplingButton(
    className = className,
    onClick = { playerSelections.map { it.copy(second = selectionValue) }.let(setPlayerSelections) }
)) { +text }

private fun ChildrenBuilder.pinSelector(
    pinSelections: List<String?>,
    setPinSelections: (List<String?>) -> Unit,
    pins: List<Pin>
) = Flipper {
    flipKey = pinSelections.generateFlipKey()
    className = styles["pinSelector"]
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


private fun ChildrenBuilder.selectedPinsDiv(children: ChildrenBuilder.() -> Unit) = cssDiv(
    attrs = { classes = classes + "${styles["selectedPins"]}" },
    css = {
        margin(5.px)
        flex(1.0)
    }
) {
    children()
}

private fun ChildrenBuilder.deselectedPinsDiv(children: ChildrenBuilder.() -> Unit) = cssDiv(
    attrs = { classes = classes + "${styles["deselectedPins"]}" },
    css = {
        flex(1.0)
        margin(5.px)
        backgroundColor = Color("#de8286")
        borderRadius = 15.px
    }
) {
    children()
}

private fun List<Pin>.selectByIds(pinSelections: List<String?>) = filter { pinSelections.contains(it.id) }

private fun List<Pin>.removeByIds(pinSelections: List<String?>) = filterNot { pinSelections.contains(it.id) }

private fun ChildrenBuilder.flippedPinButton(pin: Pin, onClick: () -> Unit = {}) = Flipped {
    flipId = pin.id
    cssDiv(attrs = { key = pin.id ?: "" }, css = { display = Display.inlineBlock }) {
        child(PinButton(pin, PinButtonScale.Small, showTooltip = true, onClick = onClick))
    }
}

private fun List<String?>.generateFlipKey() = joinToString(",") { it ?: "null" }

private fun ChildrenBuilder.spinButton(generateNewPairsFunc: () -> Unit, enabled: Boolean) = child(
    CouplingButton(
        supersize,
        pink,
        styles["spinButton"],
        onClick = generateNewPairsFunc,
        attrs = { disabled = !enabled }
    ) {
        marginBottom = 10.px
        animation("pulsate", 2.s, iterationCount = IterationCount.infinite)
    }
) {
    +"Spin!"
}

private fun ChildrenBuilder.selectablePlayerCardList(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit
) = playerSelections.map { (player, isSelected) ->
    cssDiv(css = { paddingBottom = 30.px; display = Display.inlineBlock }) {
        child(playerCard(player, isSelected, setPlayerSelections, playerSelections))
    }
}

private fun playerCard(
    player: Player,
    isSelected: Boolean,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
    playerSelections: List<Pair<Player, Boolean>>
) = PlayerCard(
    player,
    className = styles["playerCard"],
    onClick = {
        setPlayerSelections(
            flipSelectionForPlayer(player, isSelected, playerSelections)
        )
    },
    deselected = !isSelected
)

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
