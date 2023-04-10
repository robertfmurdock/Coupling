package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.aboutPageContent
import com.zegreatrob.coupling.client.party.PartyConfigContent
import com.zegreatrob.coupling.components.Controls
import com.zegreatrob.coupling.components.DispatchFunc
import com.zegreatrob.coupling.components.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.components.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.components.pairassignments.PairAssignments
import com.zegreatrob.coupling.components.pin.PinConfigContent
import com.zegreatrob.coupling.components.player.PlayerConfigContent
import com.zegreatrob.coupling.components.spin.PrepareSpinContent
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import com.zegreatrob.testmints.action.async.SuspendAction
import csstype.Auto
import csstype.Color
import csstype.Display
import csstype.FontWeight
import csstype.Globals
import csstype.NamedColor
import csstype.None
import csstype.Padding
import csstype.Position
import csstype.Visibility
import csstype.deg
import csstype.integer
import csstype.px
import csstype.rotate
import csstype.string
import csstype.vw
import emotion.react.css
import js.core.jso
import kotlinx.browser.document
import popper.core.Popper
import popper.core.ReferenceElement
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
import web.html.HTMLElement
import kotlin.js.Json

data class DemoPageFrame(val state: DemoAnimationState) : DataPropsBind<DemoPageFrame>(demoPageFrame)

private val demoPageFrame = tmFC<DemoPageFrame> { (state) ->
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

private fun ChildrenBuilder.popperDiv(
    popperRef: MutableRefObject<HTMLElement>,
    arrowRef: MutableRefObject<HTMLElement>,
    state: DemoAnimationState,
    popperInstance: PopperInstance,
) = div {
    css {
        if (state.description.isBlank()) display = None.none
    }
    div {
        css {
            background = Color("#333333eb")
            color = NamedColor.white
            fontWeight = FontWeight.bold
            padding = Padding(4.px, 8.px)
            fontSize = 24.px
            borderRadius = 20.px
            width = 400.px
            zIndex = integer(200)
            display = Display.inlineBlock

            "h2" {
                fontSize = 30.px
            }
        }
        ref = popperRef
        style = popperInstance.styles[Popper]

        +popperInstance.attributes[Popper]
        Markdown { +state.description }
        if (state.showReturnButton) {
            returnToCouplingButton()
        }
        div {
            css {
                visibility = Visibility.hidden
                position = Position.absolute
                width = 8.px
                height = 8.px
                background = Globals.inherit
                when (
                    popperInstance.attributes[Popper]
                        ?.unsafeCast<Json>()
                        ?.get("data-popper-placement")
                ) {
                    "top" -> bottom = (-4).px
                    "bottom", "bottom-start" -> top = (-4).px
                    "left" -> right = (-4).px
                    "right" -> left = (-4).px
                }
                before {
                    position = Position.absolute
                    width = 8.px
                    height = 8.px
                    background = Globals.inherit
                    visibility = Visibility.visible
                    content = string("''")
                    transform = rotate(45.deg)
                    top = 0.px
                    left = 0.px
                }
            }
            ref = arrowRef
            style = popperInstance.styles[Arrow]
            +popperInstance.attributes[Arrow]
        }
    }
}

private fun ChildrenBuilder.partyConfigFrame(state: MakeParty) {
    add(PartyConfigContent(state.party, true, {}, {}, {}))
}

private fun ChildrenBuilder.prepareSpinFrame(state: PrepareToSpin) {
    val (party, players, pins) = state
    add(PrepareSpinContent(party, players, pins, pins.map { it.id }, {}, {}, {}))
}

private fun ChildrenBuilder.playerConfigFrame(state: AddPlayer) = add(
    PlayerConfigContent(state.party, state.newPlayer, state.players, {}, {}, {}),
)

private fun ChildrenBuilder.pinConfigFrame(state: AddPin) = add(
    PinConfigContent(state.party, state.newPin, state.pins, {}, {}, {}),
)

private fun ChildrenBuilder.pairAssignmentsFrame(state: CurrentPairs) = add(
    PairAssignments(
        state.party,
        state.players,
        state.pairAssignments,
        { },
        Controls(noOpDispatchFunc) {},
        CouplingSocketMessage("", emptySet()),
        state.allowSave,
    ),
)

private val noOpDispatchFunc = object : DispatchFunc<NoOpDispatcher> {
    override fun <C : SuspendAction<NoOpDispatcher, R>, R> invoke(
        commandFunc: () -> C,
        response: (R) -> Unit,
    ): () -> Unit = {}
}

interface NoOpDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    NewPairAssignmentsCommandDispatcher
