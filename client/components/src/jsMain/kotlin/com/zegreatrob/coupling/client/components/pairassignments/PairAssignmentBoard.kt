package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotools.types.collection.NotEmptyList
import org.w3c.dom.HTMLElement
import popper.core.Placement
import popper.core.Popper
import popper.core.PositioningStrategy
import popper.core.ReferenceElement
import react.ChildrenBuilder
import react.Fragment
import react.Key
import react.Props
import react.ReactNode
import react.create
import react.dom.createPortal
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.popper.UsePopperOptions
import react.popper.usePopper
import react.useEffect
import react.useId
import react.useRef
import react.useRefCallback
import react.useState
import web.aria.AriaRole
import web.aria.status
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.LineStyle
import web.cssom.Position
import web.cssom.WhiteSpace
import web.cssom.integer
import web.cssom.pct
import web.cssom.px
import web.dom.ElementId
import web.html.ButtonType
import web.html.button
import web.dom.document as webDocument
import web.html.HTMLElement as WebHTMLElement

external interface PairAssignmentBoardProps : Props {
    var party: PartyDetails
    var pairAssignments: PairingSet
    var setPairAssignments: (PairingSet) -> Unit
    var allowSave: Boolean
}

internal val PairAssignmentBoard by nfc<PairAssignmentBoardProps> { (party, pairAssignments, setPairAssignments, allowSave) ->
    val (selection, setSelection) = useState<AssignmentSelection?>(null)
    val swap = selection as? PlayerSwap
    val pinMove = selection as? PinMove
    val (announcement, setAnnouncement) = useState("")
    val actionId = useId()
    val focusAfterUpdate = useRef<String>()
    val playerButtonId = { playerId: PlayerId -> "$actionId-player-${playerId.value}" }
    val pinButtonId = { pinId: PinId -> "$actionId-pin-${pinId.value}" }
    val sourcePair = swap?.let { pairAssignments.pairs.findPairContainingPlayer(it.player.id) }
    fun finishSelection(buttonId: String) {
        focusAfterUpdate.current = buttonId
        setSelection(null)
    }
    fun openAction(buttonId: String, nextSelection: AssignmentSelection) {
        focusAfterUpdate.current = "$buttonId-action"
        setSelection(nextSelection)
        setAnnouncement("")
    }
    fun chooseDestination(buttonId: String, nextSelection: AssignmentSelection, prompt: String) {
        focusAfterUpdate.current = buttonId
        setSelection(nextSelection)
        setAnnouncement(prompt)
    }
    useEffect(announcement, selection) {
        if (selection == null && announcement.isNotEmpty()) {
            delay(5_000)
            setAnnouncement("")
        }
    }
    useEffect {
        focusAfterUpdate.current?.let { id ->
            focusAfterUpdate.current = null
            (document.getElementById(id) as? HTMLElement)?.focus()
        }
    }
    div {
        onKeyDown = { event ->
            if (event.key == "Escape" && selection != null) {
                event.preventDefault()
                val id = when (selection) {
                    is PlayerSwap -> playerButtonId(selection.player.id)
                    is PinMove -> pinButtonId(selection.pin.id)
                }
                finishSelection(id)
                setAnnouncement("")
            }
        }
        pairAssignmentList(
            party = party,
            pairAssignments = pairAssignments,
            setPairAssignments = setPairAssignments,
            allowSave = allowSave,
            onDragStarted = {
                setSelection(null)
                setAnnouncement("")
            },
            playerAction = { pair, player ->
                if (pinMove?.choosingTarget == true) {
                    null
                } else {
                    val id = player.player.id
                    val buttonId = playerButtonId(id)
                    val menuOpen = swap?.player?.id == id && !swap.choosingTarget
                    val choosingTarget = swap?.choosingTarget == true && sourcePair != null
                    val validTarget = choosingTarget && sourcePair != pair
                    val selected = choosingTarget && swap.player.id == id
                    assignmentCardAction(
                        label = player.player.name,
                        actionLabel = "Swap with…",
                        buttonId = buttonId,
                        menuOpen = menuOpen,
                        selected = selected,
                        validTarget = validTarget,
                        onActivate = {
                            when {
                                validTarget -> {
                                    setPairAssignments(pairAssignments.copyWithSwappedPlayers(swap.player.id, player, pair))
                                    finishSelection(playerButtonId(swap.player.id))
                                    setAnnouncement("${swap.player.name} and ${player.player.name} swapped.")
                                }

                                menuOpen || selected -> {
                                    finishSelection(buttonId)
                                    setAnnouncement("")
                                }

                                !choosingTarget -> openAction(buttonId, PlayerSwap(player.player))
                            }
                        },
                        onChooseAction = {
                            chooseDestination(
                                buttonId,
                                PlayerSwap(player.player, choosingTarget = true),
                                "Choose a player in another pair to swap with ${player.player.name}, or select the same card to cancel.",
                            )
                        },
                    )
                }
            },
            pinAction = { pin ->
                val buttonId = pinButtonId(pin.id)
                val source = pinMove?.pin?.id == pin.id
                val choosingTarget = pinMove?.choosingTarget == true
                if (choosingTarget && !source) {
                    null
                } else {
                    assignmentCardAction(
                        label = pin.assignmentLabel,
                        actionLabel = "Move to…",
                        buttonId = buttonId,
                        menuOpen = source && !choosingTarget,
                        selected = source && choosingTarget,
                        onActivate = {
                            if (source) {
                                finishSelection(buttonId)
                                setAnnouncement("")
                            } else {
                                openAction(buttonId, PinMove(pin))
                            }
                        },
                        onChooseAction = {
                            chooseDestination(
                                buttonId,
                                PinMove(pin, choosingTarget = true),
                                "Choose a pair for ${pin.assignmentLabel}, or select the pin again to cancel.",
                            )
                        },
                    )
                }
            },
            pinDestination = { pair ->
                if (pinMove?.choosingTarget == true && pair.pins.none { it.id == pinMove.pin.id }) {
                    val pairLabel = pair.pinnedPlayers.toList().joinToString(" and ") { it.player.name }
                    assignmentCardButton(
                        label = "Move ${pinMove.pin.assignmentLabel} to $pairLabel",
                        validTarget = true,
                        onActivate = {
                            val result = pairAssignments.copyWithDroppedPin(pinMove.pin.id, pair.toPair().pairId)
                            if (result != pairAssignments) {
                                setPairAssignments(result)
                                setAnnouncement("${pinMove.pin.assignmentLabel} moved to $pairLabel.")
                            } else {
                                setAnnouncement("")
                            }
                            finishSelection(pinButtonId(pinMove.pin.id))
                        },
                    )
                } else {
                    null
                }
            },
        )
        if (allowSave) {
            div {
                role = AriaRole.status
                +announcement
            }
        }
    }
}

private fun ChildrenBuilder.pairAssignmentList(
    party: PartyDetails,
    pairAssignments: PairingSet,
    setPairAssignments: (PairingSet) -> Unit,
    allowSave: Boolean,
    onDragStarted: () -> Unit,
    playerAction: (PinnedCouplingPair, PinnedPlayer) -> ReactNode?,
    pinAction: (Pin) -> ReactNode?,
    pinDestination: (PinnedCouplingPair) -> ReactNode?,
) = div {
    css {
        whiteSpace = WhiteSpace.preLine
    }
    val pairs = pairAssignments.pairs.toList()
    val canEditAssignments = allowSave && pairs.size > 1
    pairs.forEachIndexed { index, pair ->
        AssignedPair(
            party = party,
            pair = pair,
            canDrag = allowSave,
            swapPlayersFunc = { player: PinnedPlayer, droppedPlayerId: PlayerId ->
                setPairAssignments(pairAssignments.copyWithSwappedPlayers(droppedPlayerId, player, pair))
            },
            pinDropFunc = { pinId: PinId, targetId: String ->
                setPairAssignments(pairAssignments.copyWithDroppedPin(pinId, targetId))
            },
            key = Key("$index"),
            playerAction = playerAction.takeIf { canEditAssignments }?.let { action -> { player -> action(pair, player) } },
            pinAction = pinAction.takeIf { canEditAssignments },
            pinDestination = if (canEditAssignments) pinDestination(pair) else null,
            onAssignmentDragStarted = onDragStarted.takeIf { canEditAssignments },
        )
    }
}

private sealed interface AssignmentSelection

private data class PlayerSwap(val player: Player, val choosingTarget: Boolean = false) : AssignmentSelection

private data class PinMove(val pin: Pin, val choosingTarget: Boolean = false) : AssignmentSelection

private val Pin.assignmentLabel get() = name.ifBlank { "Unnamed pin" }

private fun assignmentCardAction(
    label: String,
    actionLabel: String,
    buttonId: String,
    menuOpen: Boolean,
    selected: Boolean,
    validTarget: Boolean = false,
    onActivate: () -> Unit,
    onChooseAction: () -> Unit,
) = Fragment.create {
    +assignmentCardButton(label, buttonId, menuOpen, selected, validTarget, onActivate)
    if (menuOpen) {
        AssignmentActionPopover {
            triggerId = buttonId
            this.label = actionLabel
            onChoose = onChooseAction
        }
    }
}

external interface AssignmentActionPopoverProps : Props {
    var triggerId: String
    var label: String
    var onChoose: () -> Unit
}

private val AssignmentActionPopover by nfc<AssignmentActionPopoverProps> { props ->
    val (action, setAction) = useState<WebHTMLElement?>(null)
    val actionRef = useRefCallback<WebHTMLElement> { setAction(it) }
    val trigger = webDocument.getElementById(ElementId(props.triggerId))?.let { ReferenceElement(it) }
    val popper = usePopper(trigger, action, assignmentPopoverPlacement)
    +createPortal(
        button.create {
            id = ElementId("${props.triggerId}-action")
            type = ButtonType.button
            ref = actionRef
            style = popper.styles[Popper]
            +popper.attributes[Popper]
            css {
                position = Position.fixed
                zIndex = integer(200)
            }
            onClick = { props.onChoose() }
            +props.label
        },
        webDocument.body!!,
    )
}

private val assignmentPopoverPlacement = UsePopperOptions(
    placement = Placement.bottomStart,
    strategy = PositioningStrategy.fixed,
    modifiers = emptyArray(),
)

private fun assignmentCardButton(
    label: String,
    buttonId: String? = null,
    menuOpen: Boolean? = null,
    selected: Boolean = false,
    validTarget: Boolean = false,
    onActivate: () -> Unit,
) = button.create {
    id = buttonId?.let(::ElementId)
    type = ButtonType.button
    ariaLabel = label
    ariaExpanded = menuOpen
    onClick = { onActivate() }
    css {
        position = Position.absolute
        top = 0.px
        left = 0.px
        width = 100.pct
        height = 100.pct
        borderWidth = 0.px
        padding = 0.px
        backgroundColor = Color.transparent
        borderRadius = 5.px
        if (selected || validTarget) {
            boxShadow = BoxShadow(0.px, 0.px, 0.px, 3.px, Color(if (selected) "#151515" else "#23578b"))
        }
        "&:focus-visible" {
            outlineWidth = 3.px
            outlineStyle = LineStyle.solid
            outlineColor = Color("#151515")
        }
    }
}

private fun PairingSet.copyWithDroppedPin(pinId: PinId, pairId: String): PairingSet {
    val droppedPair = pairs.toList().find { it.toPair().pairId == pairId }
    val pin = findDroppedPin(pinId, this)
    return if (droppedPair != null && pin != null) {
        copy(pairs = pairs.movePinTo(pin, droppedPair))
    } else {
        this
    }
}

private fun findDroppedPin(id: PinId, pairAssignments: PairingSet) = pairAssignments
    .pairs
    .toList()
    .flatMap(PinnedCouplingPair::pins)
    .firstOrNull { it.id == id }

private fun NotEmptyList<PinnedCouplingPair>.movePinTo(pin: Pin, droppedPair: PinnedCouplingPair) = map { pair ->
    when {
        pair == droppedPair -> pair.addPin(pin)
        pair.pins.contains(pin) -> pair.removePin(pin)
        else -> pair
    }
}

private fun PinnedCouplingPair.addPin(pin: Pin) = copy(pins = pins + pin)

private fun PinnedCouplingPair.removePin(pin: Pin) = copy(pins = pins - pin)

private fun PairingSet.copyWithSwappedPlayers(
    droppedPlayerId: PlayerId,
    targetPlayer: PinnedPlayer,
    targetPair: PinnedCouplingPair,
): PairingSet {
    val sourcePair = pairs.findPairContainingPlayer(droppedPlayerId)
    val droppedPlayer = sourcePair?.pinnedPlayers?.toList()?.firstOrNull { it.player.id == droppedPlayerId }

    if (sourcePair == targetPair || droppedPlayer == null) {
        return this
    }

    return copy(
        pairs = pairs.map { pair ->
            when (pair) {
                targetPair -> pair.replacePlayer(targetPlayer, droppedPlayer)
                sourcePair -> pair.replacePlayer(droppedPlayer, targetPlayer)
                else -> pair
            }
        },
    )
}

private fun PinnedCouplingPair.replacePlayer(playerToReplace: PinnedPlayer, replacement: PinnedPlayer) = copy(
    pinnedPlayers = pinnedPlayers.map { pinnedPlayer ->
        if (pinnedPlayer == playerToReplace) {
            replacement
        } else {
            pinnedPlayer
        }
    },
)

private fun NotEmptyList<PinnedCouplingPair>.findPairContainingPlayer(droppedPlayerId: PlayerId) = toList()
    .firstOrNull { pair ->
        pair.pinnedPlayers.toList().any { player -> player.player.id == droppedPlayerId }
    }
