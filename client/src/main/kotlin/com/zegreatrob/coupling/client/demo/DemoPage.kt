package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.pink
import com.zegreatrob.coupling.client.dom.supersize
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
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import com.zegreatrob.testmints.action.async.SuspendAction
import kotlinext.js.jso
import kotlinx.browser.document
import kotlinx.css.*
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.animation
import kotlinx.css.properties.radialGradient
import kotlinx.css.properties.s
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import popper.core.Popper
import popper.core.modifier
import popper.core.modifiers.Arrow
import popper.core.modifiers.Offset
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.popper.PopperInstance
import react.popper.UsePopperOptions
import react.popper.usePopper
import react.router.dom.Link

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
        child(DemoPageFrame(currentFrame.data))
    } else {
        child(FrameRunner(demoSequence, 1.0) { state: DemoAnimationState ->
            child(DemoPageFrame(state))
        })
    }
}

data class DemoPageFrame(val state: DemoAnimationState) : DataProps<DemoPageFrame> {
    override val component = demoPageFrame
}

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
        cssDiv(css = {
            position = Position.absolute
            backgroundColor = Color("#e3002b61")
            position = Position.absolute;
            width = 100.pct
            marginLeft = LinearDimension.auto
            marginRight = LinearDimension.auto
        }) {
            +"-- DEMO MODE -- ALL BUTTONS WILL NOT WORK -- DON'T GET IT TWISTED --"
        }

        cssDiv(css = { pointerEvents = PointerEvents.none }) {
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

private fun popperOptions(arrowRef: MutableRefObject<HTMLElement>, state: DemoAnimationState): UsePopperOptions = jso {
    this.placement = state.placement
    this.modifiers = arrayOf(
        Arrow.modifier {
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
) = div {
    className = styles["popper"]
    ref = popperRef
    style = popperInstance.styles[Popper]
    +popperInstance.attributes[Popper]
    Markdown { +state.description }
    if (state.showReturnButton) {
        Link {
            this.to = "/tribes"
            child(CouplingButton(colorRuleSet = pink, sizeRuleSet = supersize, css = {
                animation("pulsate", 0.75.s, iterationCount = IterationCount.infinite)
            })) {
                couplingLogo()
            }
        }
    }
    div {
        className = styles["arrow"]
        ref = arrowRef
        style = popperInstance.styles[Arrow]
        +popperInstance.attributes[Arrow]
    }
}

fun ChildrenBuilder.couplingLogo() = cssDiv(css = {
    display = Display.flex
    alignItems = Align.center
}) {
    cssDiv(css = {
        position = Position.relative
        width = 38.px
        height = 36.px
    }) {
        cssDiv(css = {
            position = Position.absolute
            zIndex = 10
        }) { img { src = svgPath("logo") } }
        cssDiv(css = {
            position = Position.absolute
            width = 36.px
            height = 36.px
            backgroundImage = radialGradient {
                colorStop(Color.yellow)
                colorStop(Color("#ffff003d"))
                colorStop(Color("#e22092"))
            }
            borderRadius = 75.px
        })
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
