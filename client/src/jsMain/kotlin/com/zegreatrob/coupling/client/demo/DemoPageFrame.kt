package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.aboutPageContent
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.CouplingPopUp
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.external.marked.parse
import com.zegreatrob.coupling.client.components.pairassignments.PairAssignments
import com.zegreatrob.coupling.client.components.party.PartyConfigContent
import com.zegreatrob.coupling.client.components.pin.PinConfigContent
import com.zegreatrob.coupling.client.components.player.PlayerConfigContent
import com.zegreatrob.coupling.client.components.spin.PrepareSpinContent
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import com.zegreatrob.testmints.action.ActionCannon
import emotion.react.css
import js.objects.jso
import popper.core.ReferenceElement
import popper.core.modifier
import popper.core.modifiers.Arrow
import popper.core.modifiers.Offset
import react.ChildrenBuilder
import react.Props
import react.RefObject
import react.dom.html.ReactHTML.div
import react.popper.PopperInstance
import react.popper.UsePopperOptions
import react.popper.usePopper
import react.useEffect
import react.useRef
import react.useState
import web.cssom.Auto
import web.cssom.Color
import web.cssom.None
import web.cssom.Position
import web.cssom.px
import web.cssom.vw
import web.dom.document
import web.html.HTMLElement
import web.timers.setTimeout
import kotlin.time.Duration.Companion.milliseconds

external interface DemoPageFrameProps : Props {
    var state: DemoAnimationState
}

@ReactFunc
val DemoPageFrame by nfc<DemoPageFrameProps> { (state) ->
    val popperRef = useRef<HTMLElement>()
    val arrowRef = useRef<HTMLElement>()
    val (referenceElement, setReferenceElement) = useState<ReferenceElement?>(null)
    val popperInstance = usePopper(referenceElement, popperRef.current, popperOptions(arrowRef, state))

    useEffect(state) {
        setTimeout(20.milliseconds) {
            val selector = state.descriptionSelector
            val element: ReferenceElement? = if (selector.isNotBlank()) {
                document.querySelector(selector).unsafeCast<ReferenceElement>()
            } else {
                null
            }
            setReferenceElement(element)
            popperInstance.forceUpdate?.invoke()
        }
    }

    div {
        if (referenceElement != null) {
            popperDiv(popperRef, arrowRef, state, popperInstance)
        }
        div {
            css {
                position = Position.absolute
                backgroundColor = Color("#e3002b61")
                width = 100.vw
                marginLeft = Auto.auto
                marginRight = Auto.auto
            }
            +"-- DEMO MODE -- ALL BUTTONS WILL NOT WORK -- DON'T GET IT TWISTED --"
        }

        div {
            css { pointerEvents = None.none }
            when (state) {
                is Start -> aboutPageContent {
                    div { dangerouslySetInnerHTML = jso { __html = parse(state.text) } }
                }

                is ShowIntro -> aboutPageContent {
                    div { dangerouslySetInnerHTML = jso { __html = parse(state.text) } }
                }

                is MakeParty -> partyConfigFrame(state)
                is AddPlayer -> playerConfigFrame(state)
                is AddPin -> pinConfigFrame(state)
                is CurrentPairs -> pairAssignmentsFrame(state)
                is PrepareToSpin -> prepareSpinFrame(state)
            }
        }
    }
}

fun ChildrenBuilder.popperDiv(
    popperRef: RefObject<HTMLElement>,
    arrowRef: RefObject<HTMLElement>,
    state: DemoAnimationState,
    popperInstance: PopperInstance,
) = div {
    css {
        "h2" {
            fontSize = 30.px
        }
        fontSize = 24.px
        "div" { maxWidth = 400.px }
    }
    CouplingPopUp(
        hide = state.description.isBlank(),
        popperRef = popperRef,
        arrowRef = arrowRef,
        popperInstance = popperInstance,
    ) {
        div { dangerouslySetInnerHTML = jso { __html = parse(state.description) } }
        if (state.showReturnButton) {
            returnToCouplingButton()
        }
    }
}

private fun popperOptions(arrowRef: RefObject<HTMLElement>, state: DemoAnimationState): UsePopperOptions = jso {
    this.placement = state.placement
    this.modifiers = arrayOf(
        Arrow.modifier {
            this.options = jso {
                this.element = arrowRef.current
            }
        },
        Offset.modifier {
            this.options = jso { offset = Offset(0.0, 10.0) }
        },
    )
}

private fun ChildrenBuilder.partyConfigFrame(state: MakeParty) {
    PartyConfigContent(party = state.party, boost = null, isNew = true, onChange = {}, onSave = {}, onDelete = {})
}

private fun ChildrenBuilder.prepareSpinFrame(state: PrepareToSpin) {
    val (party, players, pins) = state
    PrepareSpinContent(
        party = party,
        playerSelections = players,
        pins = pins,
        pinSelections = pins.map(Pin::id),
        setPlayerSelections = {},
        setPinSelections = {},
        onSpin = {},
    )
}

private fun ChildrenBuilder.playerConfigFrame(state: AddPlayer) = PlayerConfigContent(
    party = state.party,
    boost = null,
    player = state.newPlayer,
    players = state.players,
    onChange = {},
    onSubmit = {},
    onRemove = {},
    onPlayerChange = {},
)

private fun ChildrenBuilder.pinConfigFrame(state: AddPin) = PinConfigContent(
    party = state.party,
    boost = null,
    pin = state.newPin,
    pinList = state.pins,
    onChange = {},
    onSubmit = {},
    onRemove = {},
)

private fun ChildrenBuilder.pairAssignmentsFrame(state: CurrentPairs) = PairAssignments(
    party = state.party,
    players = state.players,
    pairs = state.pairAssignments,
    setPairs = { },
    controls = Controls(noOpDispatchFunc) {},
    message = CouplingSocketMessage("", emptySet()),
    allowSave = state.allowSave,
)

private val noOpDispatchFunc = NoOpDispatcherDispatchFunc()

class NoOpDispatcherDispatchFunc : DispatchFunc<NoOpDispatcher> {
    override fun invoke(block: suspend ActionCannon<NoOpDispatcher>.() -> Unit): () -> Unit = fun() {}
}

interface NoOpDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    SpinCommand.Dispatcher
