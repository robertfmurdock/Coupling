package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.client.components.contributor.ContributorMenu
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import popper.core.Modifier
import popper.core.ModifierPhases
import popper.core.Placement
import popper.core.PositioningStrategy
import popper.core.ReferenceElement
import popper.core.modifiers.Arrow
import popper.core.modifiers.ArrowOptions
import popper.core.modifiers.Offset
import popper.core.modifiers.OffsetOptions
import popper.core.modifiers.Padding
import react.Props
import react.ReactNode
import react.RefObject
import react.dom.html.ReactHTML.div
import react.popper.UsePopperOptions
import react.popper.usePopper
import react.useEffect
import react.useRef
import react.useState
import web.cssom.Position
import web.cssom.em
import web.html.HTMLElement

external interface ContributionPopUpMenuProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var players: List<Player>
    var dispatchFunc: DispatchFunc<SavePlayerCommand.Dispatcher>
    var children: ((ReferenceElement, Player) -> Unit) -> ReactNode
}

@ReactFunc
val ContributionPopUpMenu by nfc<ContributionPopUpMenuProps> { props ->
    val (partyId, players, dispatchFunc, child) = props
    val (menuTarget, setMenuTarget) = useState<Pair<ReferenceElement, Player>?>(null)
    val popperRef = useRef<HTMLElement>()
    val arrowRef = useRef<HTMLElement>()
    val popperInstance = usePopper(menuTarget?.first, popperRef.current, popperOptions(arrowRef))

    useEffect(players) { setMenuTarget(null) }

    CouplingPopUp(
        hide = menuTarget == null,
        popperRef = popperRef,
        arrowRef = arrowRef,
        popperInstance = popperInstance,
    ) {
        div {
            css {
                position = Position.fixed
                right = 1.em
            }
            CloseButton { onClose = { setMenuTarget(null) } }
        }
        if (menuTarget != null) {
            ContributorMenu(menuTarget.second, players, partyId, dispatchFunc)
        }
    }
    +child { element, player ->
        setMenuTarget(element to player)
        popperInstance.forceUpdate?.invoke()
    }
}

private fun popperOptions(arrowRef: RefObject<HTMLElement>): UsePopperOptions = UsePopperOptions(
    placement = Placement.right,
    strategy = PositioningStrategy.absolute,
    modifiers = arrayOf(
        Modifier<ArrowOptions>(
            name = Arrow,
            options = ArrowOptions(
                element = arrowRef.current,
                padding = Padding(0.0),
            ),
            phase = ModifierPhases.main,
            enabled = true,
            fn = { null },
            effect = { null },
        ),
        Modifier<OffsetOptions>(
            name = Offset,
            options = OffsetOptions.invoke(Offset(0, 10)),
            phase = ModifierPhases.main,
            enabled = true,
            fn = { null },
            effect = { null },
        ),
    ),
)
