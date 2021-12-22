package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.pink
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipped
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipper
import com.zegreatrob.coupling.client.pin.PinButton
import com.zegreatrob.coupling.client.pin.PinButtonScale
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.client.tribe.TribeBrowser
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.*
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.animation
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.s
import kotlinx.html.classes
import react.RBuilder
import react.buildElement
import react.dom.*
import styled.css
import styled.styledDiv

private val styles = useStyles("PrepareSpin")

data class PrepareSpinContent(
    var tribe: Tribe,
    var playerSelections: List<Pair<Player, Boolean>>,
    var pins: List<Pin>,
    var pinSelections: List<String?>,
    var setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit,
    var setPinSelections: (List<String?>) -> Unit,
    var onSpin: () -> Unit
) : DataProps<PrepareSpinContent> {
    override val component: TMFC<PrepareSpinContent> get() = prepareSpinContent
}

val prepareSpinContent = reactFunction<PrepareSpinContent> { props ->
    val (tribe, playerSelections, pins, pinSelections, setPlayerSelections, setPinSelections, onSpin) = props
    div(classes = styles.className) {
        div { child(TribeBrowser(tribe)) }
        div {
            div { +spinButton(onSpin) }
            +selectorAreaDiv {
                +playerSelectorDiv {
                    h1 { +"Please select players to spin." }
                    h2 { +"Tap a player to include or exclude them." }
                    +"When you're done with your selections, hit the spin button above!"
                    styledDiv {
                        css { margin(10.px, null) }
                        +selectAllButton(playerSelections, setPlayerSelections)
                        +selectNoneButton(playerSelections, setPlayerSelections)
                    }
                    selectablePlayerCardList(playerSelections, setPlayerSelections, tribe)
                        .forEach(::child)
                }
                if (pins.isNotEmpty()) {
                    +pinSelectorDiv {
                        h1 { br {} }
                        h2 { +"Also, Pins." }
                        +"Tap any pin to skip."
                        child(pinSelector(pinSelections, setPinSelections, pins))
                    }
                }
            }
        }
    }
}

private fun selectorAreaDiv(children: RBuilder.() -> Unit) = buildElement {
    styledDiv {
        css {
            display = Display.flex
            borderSpacing = 5.px
            borderCollapse = BorderCollapse.separate
        }
        children()
    }
}

private fun playerSelectorDiv(children: RBuilder.() -> Unit) = buildElement {
    styledDiv {
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
}

private fun pinSelectorDiv(children: RBuilder.() -> Unit) = buildElement {
    styledDiv {
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
}

private fun selectAllButton(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit
) = batchSelectButton(styles["selectAllButton"], "All in!", playerSelections, setPlayerSelections, true)

private fun selectNoneButton(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit
) = batchSelectButton(styles["selectNoneButton"], "All out!", playerSelections, setPlayerSelections, false)

private fun batchSelectButton(
    className: String,
    text: String,
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (value: List<Pair<Player, Boolean>>) -> Unit,
    selectionValue: Boolean
) = CouplingButton(className = className,
        onClick = { playerSelections.map { it.copy(second = selectionValue) }.let(setPlayerSelections) }
    ) { +text }
    .create()

private fun pinSelector(pinSelections: List<String?>, setPinSelections: (List<String?>) -> Unit, pins: List<Pin>) =
    buildElement {
        flipper(flipKey = pinSelections.generateFlipKey(), classes = styles["pinSelector"]) {
            +selectedPinsDiv {
                pins.selectByIds(pinSelections)
                    .map { pin ->
                        +flippedPinButton(pin) { setPinSelections(pinSelections - pin.id) }
                    }
            }
            +deselectedPinsDiv {
                pins.removeByIds(pinSelections)
                    .map { pin ->
                        +flippedPinButton(pin) { setPinSelections(pinSelections + pin.id) }
                    }
            }
        }
    }

private fun selectedPinsDiv(children: RBuilder.() -> Unit) = buildElement {
    styledDiv {
        attrs { classes = classes + styles["selectedPins"] }
        css {
            margin(5.px)
            flex(1.0)
        }
        children()
    }
}

private fun deselectedPinsDiv(children: RBuilder.() -> Unit) = buildElement {
    styledDiv {
        attrs { classes = classes + styles["deselectedPins"] }
        css {
            flex(1.0)
            margin(5.px)
            backgroundColor = Color("#de8286")
            borderRadius = 15.px
        }
        children()
    }
}

private fun List<Pin>.selectByIds(pinSelections: List<String?>) = filter { pinSelections.contains(it.id) }

private fun List<Pin>.removeByIds(pinSelections: List<String?>) = filterNot { pinSelections.contains(it.id) }

private fun flippedPinButton(pin: Pin, onClick: () -> Unit = {}) = buildElement {
    flipped(pin.id) {
        styledDiv {
            attrs { key = pin.id ?: "" }
            css { display = Display.inlineBlock }
            child(PinButton(pin, PinButtonScale.Small, showTooltip = true, onClick = onClick))
        }
    }
}

private fun List<String?>.generateFlipKey() = joinToString(",") { it ?: "null" }

private fun spinButton(generateNewPairsFunc: () -> Unit) = CouplingButton(
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
) { +"Spin!" }.create()


private fun selectablePlayerCardList(
    playerSelections: List<Pair<Player, Boolean>>,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
    tribe: Tribe
) = playerSelections.map { (player, isSelected) ->
    buildElement {
        styledDiv {
            css { paddingBottom = 30.px; display = Display.inlineBlock }
            child(playerCard(tribe, player, isSelected, setPlayerSelections, playerSelections))
        }
    }
}

private fun playerCard(
    tribe: Tribe,
    player: Player,
    isSelected: Boolean,
    setPlayerSelections: (List<Pair<Player, Boolean>>) -> Unit,
    playerSelections: List<Pair<Player, Boolean>>
) = PlayerCard(
    tribe.id,
    player,
    className = styles["playerCard"],
    deselected = !isSelected,
    onClick = {
        setPlayerSelections(
            flipSelectionForPlayer(player, isSelected, playerSelections)
        )
    }
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
