package com.zegreatrob.coupling.client.components.spin

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.components.pairassignments.PinReminder
import com.zegreatrob.coupling.client.components.party.PartyBrowser
import com.zegreatrob.coupling.client.components.pin.PinButton
import com.zegreatrob.coupling.client.components.pin.PinButtonScale
import com.zegreatrob.coupling.client.components.pink
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.player.create
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import js.objects.unsafeJso
import react.ChildrenBuilder
import react.Props
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.span
import web.cssom.AnimationIterationCount
import web.cssom.BorderCollapse
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.Position
import web.cssom.TransitionProperty
import web.cssom.TransitionTimingFunction
import web.cssom.em
import web.cssom.ident
import web.cssom.number
import web.cssom.px
import web.cssom.rgb
import web.cssom.s

external interface PrepareSpinContentProps : Props {
    var party: PartyDetails
    var playerSelections: List<Pair<Player, Boolean>>
    var pins: List<Pin>
    var pinSelections: List<PinId>
    var setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit
    var selectPin: (PinId, Boolean) -> Unit
    var onSpin: (() -> Unit)?
}

@ReactFunc
val PrepareSpinContent by nfc<PrepareSpinContentProps> { props ->
    val (party, playerSelections, pins, pinSelections, setPlayerSelections, selectPin, onSpin) = props

    div {
        PageFrame(Color("#ff8c00"), backgroundColor = Color("#faf0d2")) {
            div { PartyBrowser(party) }
            div {
                div {
                    spinButton(onSpin)
                    if (onSpin == null) {
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
                            pinSelector(pinSelections, selectPin, pins)
                        }
                    } else {
                        div {
                            css { width = 20.em }
                            PinReminder()
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

val playerSelectorClass = ClassName {
    display = Display.inlineBlock
    flex = number(1.0)
    margin = 5.px
    borderRadius = 20.px
    padding = 5.px
    backgroundColor = Color("#fffbed")
    boxShadow = BoxShadow(1.px, 1.px, 3.px, rgb(0, 0, 0, 0.6))
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
        boxShadow = BoxShadow(1.px, 1.px, 3.px, rgb(0, 0, 0, 0.6))
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
) = CouplingButton {
    key = text
    onClick = { playerSelections.map { it.copy(second = selectionValue) }.let(setPlayerSelections) }
    +text
}

private fun ChildrenBuilder.pinSelector(
    pinSelections: List<PinId>,
    selectPin: (PinId, Boolean) -> Unit,
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
            .forEach { pin ->
                flippedPinButton(pin) { selectPin(pin.id, false) }
            }
    }
    deselectedPinsDiv {
        pins.removeByIds(pinSelections)
            .forEach { pin ->
                flippedPinButton(pin) { selectPin(pin.id, true) }
            }
    }
}

val selectedPinsClass = ClassName {
    margin = (5.px)
    flex = number(1.0)
}

private fun ChildrenBuilder.selectedPinsDiv(children: ChildrenBuilder.() -> Unit) = div {
    className = selectedPinsClass
    asDynamic()["data-selected-pins"] = ""
    children()
}

val deselectedPinsClass = ClassName {
    flex = number(1.0)
    margin = (5.px)
    backgroundColor = Color("#de8286")
    borderRadius = 15.px
}

private fun ChildrenBuilder.deselectedPinsDiv(children: ChildrenBuilder.() -> Unit) = div {
    className = deselectedPinsClass
    children()
}

private fun List<Pin>.selectByIds(pinSelections: List<PinId?>) = filter { pinSelections.contains(it.id) }

private fun List<Pin>.removeByIds(pinSelections: List<PinId?>) = filterNot { pinSelections.contains(it.id) }

private fun ChildrenBuilder.flippedPinButton(pin: Pin, onClick: () -> Unit = {}) = Flipped {
    flipId = pin.id.value.toString()
    key = pin.id.value.toString()
    div {
        key = pin.id.value.toString()
        css { display = Display.inlineBlock }
        PinButton(pin, PinButtonScale.Small, showTooltip = true, onClick = onClick)
    }
}

private fun List<PinId>.generateFlipKey() = joinToString(",") { it.value.toString() }

private fun ChildrenBuilder.spinButton(generateNewPairsFunc: (() -> Unit)?) = CouplingButton {
    sizeRuleSet = supersize
    colorRuleSet = pink
    onClick = generateNewPairsFunc
    buttonProps = unsafeJso {
        disabled = (generateNewPairsFunc == null)
    }
    className = ClassName {
        marginBottom = 10.px
        animationName = ident("pulsate")
        animationDuration = 2.s
        animationIterationCount = AnimationIterationCount.infinite
    }
    +"Spin!"
}

private fun ChildrenBuilder.selectablePlayerCardList(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
) = playerSelections.forEach { (player, isSelected) ->
    div {
        key = player.id.value.toString()
        css {
            paddingBottom = 30.px
            display = Display.inlineBlock
        }
        +playerCard(player, isSelected, setPlayerSelections, playerSelections)
    }
}

private fun playerCard(
    player: Player,
    isSelected: Boolean,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
    playerSelections: List<Pair<Player, Boolean>>,
) = PlayerCard.create(
    player,
    className = ClassName {
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
