package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.client.aboutPageContent
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.components.external.reactmarkdown.Markdown
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
import com.zegreatrob.testmints.action.async.SuspendAction
import emotion.react.css
import js.core.jso
import kotlinx.browser.document
import popper.core.ReferenceElement
import popper.core.modifier
import popper.core.modifiers.Arrow
import popper.core.modifiers.Offset
import react.ChildrenBuilder
import react.MutableRefObject
import react.Props
import react.dom.html.ReactHTML.div
import react.popper.UsePopperOptions
import react.popper.usePopper
import react.useLayoutEffect
import react.useRef
import react.useState
import web.cssom.Auto
import web.cssom.Color
import web.cssom.None
import web.cssom.Position
import web.cssom.vw
import web.html.HTMLElement

external interface DemoPageFrameProps : Props {
    var state: DemoAnimationState
}

@ReactFunc
val DemoPageFrame by nfc<DemoPageFrameProps> { (state) ->
    val popperRef = useRef<HTMLElement>()
    val arrowRef = useRef<HTMLElement>()
    val (referenceElement, setReferenceElement) = useState<ReferenceElement?>(null)
    val popperInstance = usePopper(referenceElement, popperRef.current, popperOptions(arrowRef, state))

    useLayoutEffect(state) {
        val className = state.descriptionSelector
        val element: ReferenceElement? = if (className.isNotBlank()) {
            document.querySelector(className).unsafeCast<ReferenceElement>()
        } else {
            null
        }
        setReferenceElement(element)
        popperInstance.forceUpdate?.invoke()
    }

    div {
        popperDiv(popperRef, arrowRef, state, popperInstance)
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
                is Start -> aboutPageContent { Markdown { +state.text } }
                is ShowIntro -> aboutPageContent { Markdown { +state.text } }
                is MakeParty -> partyConfigFrame(state)
                is AddPlayer -> playerConfigFrame(state)
                is AddPin -> pinConfigFrame(state)
                is CurrentPairs -> pairAssignmentsFrame(state)
                is PrepareToSpin -> prepareSpinFrame(state)
            }
        }
    }
}

private fun popperOptions(arrowRef: MutableRefObject<HTMLElement>, state: DemoAnimationState): UsePopperOptions = jso {
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
    PartyConfigContent(state.party, true, {}, {}, {})
}

private fun ChildrenBuilder.prepareSpinFrame(state: PrepareToSpin) {
    val (party, players, pins) = state
    PrepareSpinContent(party, players, pins, pins.map(Pin::id), {}, {}, {})
}

private fun ChildrenBuilder.playerConfigFrame(state: AddPlayer) =
    PlayerConfigContent(state.party, state.newPlayer, state.players, {}, {}, {})

private fun ChildrenBuilder.pinConfigFrame(state: AddPin) =
    PinConfigContent(state.party, state.newPin, state.pins, {}, {}, {})

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
    override fun <C : SuspendAction<NoOpDispatcher, R>, R> invoke(
        commandFunc: () -> C,
        response: (R) -> Unit,
    ) = fun() {}

    override fun <C, R> invoke(
        commandFunc: () -> C,
        fireFunc: suspend ActionCannon<NoOpDispatcher>.(C) -> R,
        response: (R) -> Unit,
    ) = fun() {}

    override fun invoke(block: suspend ActionCannon<NoOpDispatcher>.() -> Unit): () -> Unit = fun() {}
}

interface NoOpDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    SpinCommand.Dispatcher
