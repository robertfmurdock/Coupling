package com.zegreatrob.coupling.client.components.spin

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.PinButton
import com.zegreatrob.coupling.client.components.PinButtonScale
import com.zegreatrob.coupling.client.components.PlayerCard
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.components.party.PartyBrowser
import com.zegreatrob.coupling.client.components.pink
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.AnimationIterationCount
import csstype.BorderCollapse
import csstype.BoxShadow
import csstype.Color
import csstype.Display
import csstype.FlexDirection
import csstype.Position
import csstype.TransitionProperty
import csstype.TransitionTimingFunction
import csstype.em
import csstype.ident
import csstype.number
import csstype.px
import csstype.rgba
import csstype.s
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.span

data class PrepareSpinContent(
    var party: Party,
    var playerSelections: List<Pair<Player, Boolean>>,
    var pins: List<Pin>,
    var pinSelections: List<String?>,
    var setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit,
    var setPinSelections: (List<String?>) -> Unit,
    var onSpin: () -> Unit,
) : DataPropsBind<PrepareSpinContent>(prepareSpinContent)

val prepareSpinContent = tmFC<PrepareSpinContent> { props ->
    val (party, playerSelections, pins, pinSelections, setPlayerSelections, setPinSelections, onSpin) = props

    val enabled = playerSelections.any { it.second }

    div {
        add(PageFrame(Color("#ff8c00"), backgroundColor = Color("#faf0d2"))) {
            div { add(PartyBrowser(party)) }
            div {
                div {
                    spinButton(onSpin, enabled = enabled)
                    if (!enabled) {
                        span {
                            css {
                                position = Position.absolute
                                width = 12.em
                            }
                            +"Please tap a player to include them before spinning."
                        }
                    }
                }
                selectorAreaDiv {
                    playerSelectorDiv {
                        h1 { +"Please select players to spin." }
                        h2 { +"Tap a player to include or exclude them." }
                        +"When you're done with your selections, hit the spin button above!"
                        div {
                            css { margin = 10.px }
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

private fun ChildrenBuilder.selectorAreaDiv(children: ChildrenBuilder.() -> Unit) = div {
    css {
        display = Display.flex
        borderSpacing = 5.px
        borderCollapse = BorderCollapse.separate
    }
    children()
}

val playerSelectorClass = emotion.css.ClassName {
    display = Display.inlineBlock
    flex = number(1.0)
    margin = 5.px
    borderRadius = 20.px
    padding = 5.px
    backgroundColor = Color("#fffbed")
    boxShadow = BoxShadow(1.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
}

private fun ChildrenBuilder.playerSelectorDiv(children: ChildrenBuilder.() -> Unit) = div {
    className = playerSelectorClass
    children()
}

private fun ChildrenBuilder.pinSelectorDiv(children: ChildrenBuilder.() -> Unit) = div {
    css {
        display = Display.inlineFlex
        flexDirection = FlexDirection.column
        margin = (5.px)
        borderRadius = 20.px
        padding = (5.px)
        backgroundColor = Color("#fffbed")
        boxShadow = BoxShadow(1.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
        width = 125.px
    }
    children()
}

private fun ChildrenBuilder.selectAllButton(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit,
) = batchSelectButton("All in!", playerSelections, setPlayerSelections, true)

private fun ChildrenBuilder.selectNoneButton(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit,
) = batchSelectButton("All out!", playerSelections, setPlayerSelections, false)

private fun ChildrenBuilder.batchSelectButton(
    text: String,
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit,
    selectionValue: Boolean,
) = add(
    com.zegreatrob.coupling.client.components.CouplingButton(onClick = {
        playerSelections.map { it.copy(second = selectionValue) }.let(setPlayerSelections)
    }),
) { +text }

private fun ChildrenBuilder.pinSelector(
    pinSelections: List<String?>,
    setPinSelections: (List<String?>) -> Unit,
    pins: List<Pin>,
) = Flipper {
    flipKey = pinSelections.generateFlipKey()
    css {
        display = Display.flex
        flexDirection = FlexDirection.column
        flex = number(1.0)
    }
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

val selectedPinsClass = emotion.css.ClassName {
    margin = (5.px)
    flex = number(1.0)
}

private fun ChildrenBuilder.selectedPinsDiv(children: ChildrenBuilder.() -> Unit) = div {
    className = selectedPinsClass
    asDynamic()["data-selected-pins"] = ""
    children()
}

val deselectedPinsClass = emotion.css.ClassName {
    flex = number(1.0)
    margin = (5.px)
    backgroundColor = Color("#de8286")
    borderRadius = 15.px
}

private fun ChildrenBuilder.deselectedPinsDiv(children: ChildrenBuilder.() -> Unit) = div {
    className = deselectedPinsClass
    children()
}

private fun List<Pin>.selectByIds(pinSelections: List<String?>) = filter { pinSelections.contains(it.id) }

private fun List<Pin>.removeByIds(pinSelections: List<String?>) = filterNot { pinSelections.contains(it.id) }

private fun ChildrenBuilder.flippedPinButton(pin: Pin, onClick: () -> Unit = {}) = Flipped {
    flipId = pin.id
    div {
        key = pin.id ?: ""
        css { display = Display.inlineBlock }
        add(PinButton(pin, PinButtonScale.Small, showTooltip = true, onClick = onClick))
    }
}

private fun List<String?>.generateFlipKey() = joinToString(",") { it ?: "null" }

private fun ChildrenBuilder.spinButton(generateNewPairsFunc: () -> Unit, enabled: Boolean) = add(
    com.zegreatrob.coupling.client.components.CouplingButton(
        com.zegreatrob.coupling.client.components.supersize,
        com.zegreatrob.coupling.client.components.pink,
        onClick = generateNewPairsFunc,
        attrs = { disabled = !enabled },
    ) {
        marginBottom = 10.px
        animationName = ident("pulsate")
        animationDuration = 2.s
        animationIterationCount = AnimationIterationCount.infinite
    },
) {
    +"Spin!"
}

private fun ChildrenBuilder.selectablePlayerCardList(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
) = playerSelections.map { (player, isSelected) ->
    div {
        css { paddingBottom = 30.px; display = Display.inlineBlock }
        add(playerCard(player, isSelected, setPlayerSelections, playerSelections))
    }
}

private fun playerCard(
    player: Player,
    isSelected: Boolean,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
    playerSelections: List<Pair<Player, Boolean>>,
) = PlayerCard(
    player,
    className = emotion.css.ClassName {
        transitionProperty = TransitionProperty.all
        transitionDuration = 0.25.s
        transitionTimingFunction = TransitionTimingFunction.easeOut
    },
    onClick = {
        setPlayerSelections(
            flipSelectionForPlayer(player, isSelected, playerSelections),
        )
    },
    deselected = !isSelected,
)

private fun flipSelectionForPlayer(
    targetPlayer: Player,
    targetIsSelected: Boolean,
    playerSelections: List<Pair<Player, Boolean>>,
) = playerSelections.map { pair ->
    if (pair.first == targetPlayer) {
        Pair(targetPlayer, !targetIsSelected)
    } else {
        pair
    }
}
