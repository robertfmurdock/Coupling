package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.Paths.currentPairsPath
import com.zegreatrob.coupling.client.components.green
import com.zegreatrob.coupling.client.components.red
import com.zegreatrob.coupling.client.components.small
import com.zegreatrob.coupling.client.components.supersize
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
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.browser.document
import kotools.types.collection.NotEmptyList
import org.w3c.dom.HTMLElement
import react.ChildrenBuilder
import react.Key
import react.Props
import react.create
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.router.Navigate
import react.useEffect
import react.useId
import react.useRef
import react.useState
import web.aria.AriaRole
import web.aria.status
import web.cssom.ClassName
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

external interface CurrentPairAssignmentsPanelProps : Props {
    var party: PartyDetails
    var pairAssignments: PairingSet
    var setPairAssignments: (PairingSet) -> Unit
    var allowSave: Boolean
    var dispatchFunc: DispatchFunc<DeletePairAssignmentsCommand.Dispatcher>
}

@ReactFunc
val CurrentPairAssignmentsPanel by nfc<CurrentPairAssignmentsPanelProps> { props ->
    val (party, pairAssignments, setPairAssignments, allowSave, dispatchFunc) = props
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val (swap, setSwap) = useState<PlayerSwap?>(null)
    val (announcement, setAnnouncement) = useState("")
    val actionId = useId()
    val focusAfterUpdate = useRef<String>()
    val playerButtonId = { playerId: PlayerId -> "$actionId-${playerId.value}" }
    val sourcePair = swap?.let { pairAssignments.pairs.findPairContainingPlayer(it.player.id) }
    val finishSwap = { playerId: PlayerId ->
        focusAfterUpdate.current = playerButtonId(playerId)
        setSwap(null)
    }
    useEffect {
        focusAfterUpdate.current?.let { id ->
            focusAfterUpdate.current = null
            (document.getElementById(id) as? HTMLElement)?.focus()
        }
    }
    val redirectToCurrentFunc = { setRedirectUrl(party.id.currentPairsPath()) }
    val onCancel = dispatchFunc {
        fire(DeletePairAssignmentsCommand(party.id, pairAssignments.id))
        redirectToCurrentFunc()
    }
    if (redirectUrl != null) {
        Navigate { to = redirectUrl }
    } else {
        div {
            className = ClassName("current-pair-assignments")
            dateHeader(pairAssignments)
            div {
                onKeyDown = { event ->
                    if (event.key == "Escape" && swap != null) {
                        event.preventDefault()
                        finishSwap(swap.player.id)
                        setAnnouncement("")
                    }
                }
                pairAssignmentList(
                    party,
                    pairAssignments,
                    setPairAssignments,
                    allowSave,
                    onPlayerDragStarted = {
                        setSwap(null)
                        setAnnouncement("")
                    },
                ) { pair, player ->
                    val id = player.player.id
                    val menuOpen = swap?.player?.id == id && !swap.choosingTarget
                    val choosingTarget = swap?.choosingTarget == true && sourcePair != null
                    val validTarget = choosingTarget && sourcePair != pair
                    playerSwapAction(
                        player = player.player,
                        buttonId = playerButtonId(id),
                        menuOpen = menuOpen,
                        selected = choosingTarget && swap?.player?.id == id,
                        validTarget = validTarget,
                        onActivate = {
                            when {
                                validTarget -> {
                                    setPairAssignments(pairAssignments.copyWithSwappedPlayers(swap.player.id, player, pair))
                                    finishSwap(swap.player.id)
                                    setAnnouncement("${swap.player.name} and ${player.player.name} swapped.")
                                }

                                !choosingTarget -> {
                                    setAnnouncement("")
                                    if (menuOpen) {
                                        finishSwap(id)
                                    } else {
                                        focusAfterUpdate.current = "${playerButtonId(id)}-swap"
                                        setSwap(PlayerSwap(player.player))
                                    }
                                }
                            }
                        },
                        onChooseSwap = {
                            focusAfterUpdate.current = playerButtonId(id)
                            setSwap(PlayerSwap(player.player, choosingTarget = true))
                            setAnnouncement("Choose a player in another pair to swap with ${player.player.name}.")
                        },
                    )
                }
                if (allowSave) {
                    div {
                        role = AriaRole.status
                        +announcement
                    }
                }
            }
            if (allowSave) {
                div {
                    saveButton(redirectToCurrentFunc)
                    cancelButton(onCancel)
                }
            }
        }
    }
}

private fun ChildrenBuilder.dateHeader(pairAssignments: PairingSet) = div {
    div { PairAssignmentsHeader(pairAssignments) }
}

private fun ChildrenBuilder.pairAssignmentList(
    party: PartyDetails,
    pairAssignments: PairingSet,
    setPairAssignments: (PairingSet) -> Unit,
    allowSave: Boolean,
    onPlayerDragStarted: () -> Unit,
    playerAction: (PinnedCouplingPair, PinnedPlayer) -> react.ReactNode,
) = div {
    css {
        whiteSpace = WhiteSpace.preLine
    }
    val pairs = pairAssignments.pairs.toList()
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
            playerAction = playerAction.takeIf { pairs.size > 1 }?.let { action -> { player -> action(pair, player) } },
            onPlayerDragStarted = onPlayerDragStarted.takeIf { pairs.size > 1 },
        )
    }
}

private data class PlayerSwap(val player: Player, val choosingTarget: Boolean = false)

private fun playerSwapAction(
    player: Player,
    buttonId: String,
    menuOpen: Boolean,
    selected: Boolean,
    validTarget: Boolean,
    onActivate: () -> Unit,
    onChooseSwap: () -> Unit,
) = react.Fragment.create {
    button {
        id = ElementId(buttonId)
        type = ButtonType.button
        ariaLabel = player.name
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
                boxShadow = web.cssom.BoxShadow(0.px, 0.px, 0.px, 3.px, Color(if (selected) "#151515" else "#23578b"))
            }
            "&:focus-visible" {
                outlineWidth = 3.px
                outlineStyle = LineStyle.solid
                outlineColor = Color("#151515")
            }
        }
    }
    if (menuOpen) {
        button {
            id = ElementId("$buttonId-swap")
            type = ButtonType.button
            css {
                position = Position.absolute
                top = 100.pct
                left = 0.px
                zIndex = integer(1)
            }
            onClick = { onChooseSwap() }
            +"Swap with…"
        }
    }
}

private fun PairingSet.copyWithDroppedPin(pinId: PinId, pairId: String): PairingSet {
    val droppedPair = pairs.toList().find { it.toPair().pairId == pairId }
    return if (droppedPair != null) {
        copy(pairs = pairs.movePinTo(findDroppedPin(pinId, this), droppedPair))
    } else {
        this
    }
}

private fun findDroppedPin(id: PinId, pairAssignments: PairingSet) = pairAssignments
    .pairs
    .toList()
    .flatMap(PinnedCouplingPair::pins)
    .first { it.id == id }

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

private fun ChildrenBuilder.saveButton(onSave: () -> Unit) = CouplingButton {
    sizeRuleSet = supersize
    colorRuleSet = green
    onClick = onSave
    css { margin = 4.px }
    +"Save!"
}

private fun ChildrenBuilder.cancelButton(onCancel: () -> Unit) = CouplingButton {
    sizeRuleSet = small
    colorRuleSet = red
    onClick = onCancel
    +"Cancel"
}
