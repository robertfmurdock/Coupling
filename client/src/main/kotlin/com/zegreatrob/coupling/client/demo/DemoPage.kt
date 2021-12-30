package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.FrameRunner
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactmarkdown.Markdown
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.PairAssignments
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpinContent
import com.zegreatrob.coupling.client.pin.PinCommandDispatcher
import com.zegreatrob.coupling.client.pin.PinConfigContent
import com.zegreatrob.coupling.client.player.PlayerConfigContent
import com.zegreatrob.coupling.client.player.PlayerConfigDispatcher
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.tribe.TribeConfigContent
import com.zegreatrob.coupling.client.tribe.TribeConfigDispatcher
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.minreact.child
import com.zegreatrob.testmints.action.async.SuspendAction
import kotlinext.js.jso
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import popper.core.Placement
import popper.core.Popper
import popper.core.modifier
import popper.core.modifiers.Arrow
import popper.core.modifiers.Offset
import react.*
import react.dom.html.ReactHTML.div
import react.popper.PopperInstance
import react.popper.UsePopperOptions
import react.popper.usePopper

interface NoOpDispatcher : TribeConfigDispatcher, PlayerConfigDispatcher, PinCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher, NewPairAssignmentsCommandDispatcher {
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

private val noOpDispatchFunc = object : DispatchFunc<NoOpDispatcher> {
    override fun <C : SuspendAction<NoOpDispatcher, R>, R> invoke(
        commandFunc: () -> C, response: (R) -> Unit
    ): () -> Unit = {}
}

val styles = useStyles("DemoPage")

val demoSequence by kotlin.lazy { DemoAnimationState.generateSequence() }

val DemoPage = FC<PageProps> { props ->
    val frameIndex = props.search.get("frame")
    val currentFrame = frameIndex?.toIntOrNull()?.let { demoSequence.toList()[it] }
    if (currentFrame != null) {
        demoPageFrame(currentFrame.data)
    } else {
        child(FrameRunner(demoSequence, 1.0) { state: DemoAnimationState ->
            demoPageFrame(state)
        })
    }
}

private fun ChildrenBuilder.demoPageFrame(state: DemoAnimationState) {
    div {
        val popperRef = useRef<HTMLElement>()
        val arrowRef = useRef<HTMLElement>()

        val (referenceElement, setReferenceElement) = useState<Element?>(null)

        useLayoutEffect(state) {
            val className = state.descriptionSelector
            val element: Element? = if (className.isNotBlank()) document.querySelector(className) else null
            setReferenceElement(element)
        }

        val popperInstance = usePopper(referenceElement, popperRef.current, popperOptions(arrowRef))

        popperDiv(popperRef, arrowRef, state, popperInstance)

        div {
            when (state) {
                Start -> +"Starting..."
                ShowIntro -> +"Alright, here's an example of how you might use the app."
                is MakeTribe -> tribeConfigFrame(state)
                is AddPlayer -> playerConfigFrame(state)
                is AddPin -> pinConfigFrame(state)
                is CurrentPairs -> pairAssignmentsFrame(state)
                is PrepareToSpin -> prepareSpinFrame(state)
            }
        }
    }
}

private fun popperOptions(arrowRef: MutableRefObject<HTMLElement>, ): UsePopperOptions = jso {
    this.modifiers = arrayOf(
        Arrow.modifier {
            placement = Placement.right
            this.options = jso {
                this.element = arrowRef.current
            }
        },
        Offset.modifier {
            this.options = jso { offset = arrayOf(0, 10) }
        }
    )
}

private fun ChildrenBuilder.popperDiv(
    popperRef: MutableRefObject<HTMLElement>,
    arrowRef: MutableRefObject<HTMLElement>,
    state: DemoAnimationState,
    popperInstance: PopperInstance
) {
    div {
        className = styles["popper"]
        ref = popperRef
        style = popperInstance.styles[Popper]
        +popperInstance.attributes[Popper]
        Markdown { +state.description }
        div {
            className = styles["arrow"]
            ref = arrowRef
            style = popperInstance.styles[Arrow]
            +popperInstance.attributes[Arrow]
        }
    }
}

private fun ChildrenBuilder.prepareSpinFrame(state: PrepareToSpin) {
    val (tribe, players, pins) = state
    child(PrepareSpinContent(tribe, players, pins, pins.map { it.id }, {}, {}, {}))
}

private fun ChildrenBuilder.tribeConfigFrame(state: MakeTribe) {
    child(TribeConfigContent(state.tribe, true, {}, {}, {}))
}

private fun ChildrenBuilder.playerConfigFrame(state: AddPlayer) =
    child(PlayerConfigContent(state.tribe, state.newPlayer, state.players, {}, {}, {}))

private fun ChildrenBuilder.pinConfigFrame(state: AddPin) =
    child(PinConfigContent(state.tribe, state.newPin, state.pins, {}, {}, {}))

private fun ChildrenBuilder.pairAssignmentsFrame(state: CurrentPairs) = child(
    PairAssignments(
        state.tribe,
        state.players,
        state.pairAssignments,
        { },
        Controls(noOpDispatchFunc) {},
        CouplingSocketMessage("", emptySet()),
        state.allowSave
    )
)
