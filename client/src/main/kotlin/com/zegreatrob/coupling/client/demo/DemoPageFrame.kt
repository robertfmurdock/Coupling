package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.aboutPageContent
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.pairassignments.PairAssignments
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpinContent
import com.zegreatrob.coupling.client.party.PartyConfigContent
import com.zegreatrob.coupling.client.pin.PinConfigContent
import com.zegreatrob.coupling.client.player.PlayerConfigContent
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.create
import com.zegreatrob.minreact.tmFC
import com.zegreatrob.testmints.action.async.SuspendAction
import csstype.Auto
import csstype.Color
import csstype.None
import csstype.Position
import csstype.vw
import emotion.react.css
import kotlinx.browser.document
import kotlinx.js.jso
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import popper.core.Popper
import popper.core.modifier
import popper.core.modifiers.Arrow
import popper.core.modifiers.Offset
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.popper.PopperInstance
import react.popper.UsePopperOptions
import react.popper.usePopper
import react.useLayoutEffect
import react.useRef
import react.useState

data class DemoPageFrame(val state: DemoAnimationState) : DataPropsBind<DemoPageFrame>(demoPageFrame)

private val demoPageFrame = tmFC<DemoPageFrame> { (state) ->
    val popperRef = useRef<HTMLElement>()
    val arrowRef = useRef<HTMLElement>()

    val (referenceElement, setReferenceElement) = useState<Element?>(null)

    val popperInstance = usePopper(referenceElement, popperRef.current, popperOptions(arrowRef, state))

    useLayoutEffect(state) {
        val className = state.descriptionSelector
        val element: Element? = if (className.isNotBlank()) document.querySelector(className) else null
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
        }
    )
}

private fun ChildrenBuilder.popperDiv(
    popperRef: MutableRefObject<HTMLElement>,
    arrowRef: MutableRefObject<HTMLElement>,
    state: DemoAnimationState,
    popperInstance: PopperInstance
) = div {
    css {
        if (state.description.isBlank()) display = None.none
    }
    div {
        className = styles["popper"]
        ref = popperRef
        style = popperInstance.styles[Popper]

        +popperInstance.attributes[Popper]
        Markdown { +state.description }
        if (state.showReturnButton) {
            returnToCouplingButton()
        }
        div {
            className = styles["arrow"]
            ref = arrowRef
            style = popperInstance.styles[Arrow]
            +popperInstance.attributes[Arrow]
        }
    }
}

private fun ChildrenBuilder.partyConfigFrame(state: MakeParty) {
    +(PartyConfigContent(state.party, true, {}, {}, {}).create())
}

private fun ChildrenBuilder.prepareSpinFrame(state: PrepareToSpin) {
    val (party, players, pins) = state
    +PrepareSpinContent(party, players, pins, pins.map { it.id }, {}, {}, {}).create()
}

private fun ChildrenBuilder.playerConfigFrame(state: AddPlayer) =
    +PlayerConfigContent(state.party, state.newPlayer, state.players, {}, {}, {}).create()

private fun ChildrenBuilder.pinConfigFrame(state: AddPin) =
    +PinConfigContent(state.party, state.newPin, state.pins, {}, {}, {}).create()

private fun ChildrenBuilder.pairAssignmentsFrame(state: CurrentPairs) = +PairAssignments(
    state.party,
    state.players,
    state.pairAssignments,
    { },
    Controls(noOpDispatchFunc) {},
    CouplingSocketMessage("", emptySet()),
    state.allowSave
).create()

private val noOpDispatchFunc = object : DispatchFunc<NoOpDispatcher> {
    override fun <C : SuspendAction<NoOpDispatcher, R>, R> invoke(
        commandFunc: () -> C,
        response: (R) -> Unit
    ): () -> Unit = {}
}
