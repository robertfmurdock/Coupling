package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.client.external.react.useState
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import react.*

data class FrameRunnerProps(val sequence: Sequence<Pair<*, Int>>) : RProps

private fun <A, B, A2> Pair<A, B>.letFirst(transform: (A) -> A2) = transform(first) to second

private fun <A, B, C> Pair<A, B>.let(transform: (A, B) -> C) = transform(first, second)

private fun <I, O, O2> ((I) -> O).join(transform: O.() -> O2) = { pair: I -> this(pair).transform() }

object FrameRunner : FRComponent<FrameRunnerProps>(provider()), WindowFunctions {

    fun <T> RBuilder.frameRunner(sequence: Sequence<Pair<T, Int>>, children: RBuilder.(T) -> Unit) = child(
        createElement(component.rFunction, FrameRunnerProps(sequence), { value: T ->
            buildElements { children(value) }
        })
    )

    override fun render(props: FrameRunnerProps) = reactElement {
        val (sequence) = props
        val (state, setState) = useState(sequence.first().first)
        val scheduleStateFunc = scheduleStateFunc(setState)

        useEffect(emptyList()) { sequence.forEach(scheduleStateFunc) }
        children(state, props)
    }

    private fun scheduleStateFunc(setState: (Any?) -> Unit) = toSetTimeoutArgsFunc(setState)
        .join { let(FrameRunner::setTimeout) }
        .join { Unit }

    private fun toSetTimeoutArgsFunc(setState: (Any?) -> Unit) = { pair: Pair<*, Int> ->
        asSetTimeoutArgs(
            pair,
            curryStateToTimedFunc(setState)
        )
    }

    private fun curryStateToTimedFunc(setState: (Any?) -> Unit) = { it: Any? ->
        { setState(it) }
    }

    private fun asSetTimeoutArgs(pair: Pair<*, Int>, setStateToTimedFunc: (Any?) -> () -> Unit) =
        pair.letFirst(setStateToTimedFunc)

    private fun setTimeout(timedFunc: () -> Unit, time: Int) = window.setTimeout(timedFunc, time)

    private fun RBuilder.children(state: Any?, props: FrameRunnerProps): Any {
        val unsafeCast = props.children.unsafeCast<(Any?) -> Any>()
        val children = unsafeCast(state)
        return childList.add(children)
    }

}