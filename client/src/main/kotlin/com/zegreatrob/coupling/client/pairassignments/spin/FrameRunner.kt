package com.zegreatrob.coupling.client.pairassignments.spin

import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.useState
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import react.*

data class FrameRunnerProps(val sequence: Sequence<Pair<SpinAnimationState, Int>>) : RProps

private fun <A, B, A2> Pair<A, B>.letFirst(transform: (A) -> A2) = transform(first) to second

private fun <A, B, C> Pair<A, B>.let(transform: (A, B) -> C) = transform(first, second)

private fun <I, O, O2> ((I) -> O).join(transform: O.() -> O2) = { pair: I -> this(pair).transform() }

object FrameRunner : FRComponent<FrameRunnerProps>(
    provider()
), WindowFunctions {

    fun RBuilder.frameRunner(
        sequence: Sequence<Pair<SpinAnimationState, Int>>,
        children: RBuilder.(SpinAnimationState) -> Unit
    ) = child(
        createElement(component.rFunction, FrameRunnerProps(sequence), { value: SpinAnimationState ->
            buildElements { children(value) }
        })
    )

    override fun render(props: FrameRunnerProps) =
        reactElement {
            val (sequence) = props
            val (state, setState) = useState(sequence.first().first)
            val scheduleStateFunc = scheduleStateFunc(setState)

            useEffect(emptyList()) { sequence.forEach(scheduleStateFunc) }
            children(state, props)
        }

    private fun scheduleStateFunc(setState: (SpinAnimationState) -> Unit) = toSetTimeoutArgsFunc(setState)
        .join { let(::setTimeout) }
        .join { Unit }

    private fun toSetTimeoutArgsFunc(setState: (SpinAnimationState) -> Unit) = { pair: Pair<SpinAnimationState, Int> ->
        asSetTimeoutArgs(pair, curryStateToTimedFunc(setState))
    }

    private fun curryStateToTimedFunc(setState: (SpinAnimationState) -> Unit) = { it: SpinAnimationState ->
        { setState(it) }
    }

    private fun asSetTimeoutArgs(
        pair: Pair<SpinAnimationState, Int>,
        setStateToTimedFunc: (SpinAnimationState) -> () -> Unit
    ) = pair.letFirst(setStateToTimedFunc)

    private fun setTimeout(timedFunc: () -> Unit, time: Int) = window.setTimeout(timedFunc, time)

    private fun RBuilder.children(state: SpinAnimationState, props: FrameRunnerProps): Any {
        val unsafeCast = props.children.unsafeCast<(SpinAnimationState) -> Any>()
        val children = unsafeCast(state)
        return childList.add(children)
    }

}