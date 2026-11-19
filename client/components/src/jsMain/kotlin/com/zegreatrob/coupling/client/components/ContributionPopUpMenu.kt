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
import web.aria.AriaRole
import web.aria.dialog
import web.cssom.Position
import web.cssom.em
import web.dom.Node
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
    val contentRef = useRef<HTMLElement>()
    val popperInstance = usePopper(menuTarget?.first, popperRef.current, popperOptions(arrowRef))

    fun dismiss(restoreFocus: Boolean) {
        if (restoreFocus) {
            menuTarget?.first?.unsafeCast<HTMLElement>()?.takeIf { it.isConnected }?.focus()
        }
        setMenuTarget(null)
    }

    useEffect(players) {
        // A successful save can remove the focused action before this effect runs.
        if (menuTarget != null) dismiss(restoreFocus = true)
    }
    useEffect(menuTarget) {
        if (menuTarget != null) contentRef.current?.querySelector("button")?.unsafeCast<HTMLElement>()?.focus()
    }

    CouplingPopUp(
        hide = menuTarget == null,
        popperRef = popperRef,
        arrowRef = arrowRef,
        popperInstance = popperInstance,
    ) {
        if (menuTarget != null) {
            div {
                ref = contentRef
                role = AriaRole.dialog
                ariaModal = false
                ariaLabel = "Contributor actions for ${menuTarget.second.name}"
                onKeyDown = { event ->
                    if (event.key == "Escape") {
                        event.preventDefault()
                        event.stopPropagation()
                        dismiss(restoreFocus = true)
                    }
                }
                onBlur = { event ->
                    if (!event.currentTarget.contains(event.relatedTarget?.unsafeCast<Node>())) {
                        dismiss(restoreFocus = false)
                    }
                }
                div {
                    css {
                        position = Position.absolute
                        right = 1.em
                    }
                    CloseButton { onClose = { dismiss(restoreFocus = true) } }
                }
                ContributorMenu(menuTarget.second, players, partyId, dispatchFunc)
            }
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
        Modifier(
            name = Arrow,
            options = ArrowOptions(
                element = arrowRef.current,
                padding = Padding(0.0),
            ),
            phase = ModifierPhases.main,
            enabled = true,
        ),
        Modifier(
            name = Offset,
            options = OffsetOptions.invoke(Offset(0, 10)),
            phase = ModifierPhases.main,
            enabled = true,
        ),
    ),
)
