package com.zegreatrob.coupling.client

import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import react.*
import kotlin.math.round

data class FrameRunnerProps(val sequence: Sequence<Pair<*, Int>>, val speed: Double) : RProps

private fun <A, B, A2> Pair<A, B>.letFirst(transform: (A) -> A2) = transform(first) to second
private fun <A, B, B2> Pair<A, B>.letSecond(transform: (B) -> B2) = first to transform(second)
private fun <A, B, C> Pair<A, B>.let(transform: (A, B) -> C) = transform(first, second)
private fun <I, O, O2> ((I) -> O).join(transform: (O) -> O2) = { pair: I -> transform(this(pair)) }
private fun <I, O> ((I) -> O).curryOneArgToNoArgsFunc(): (I) -> () -> O = { it: I -> { this(it) } }

fun <T> RBuilder.frameRunner(sequence: Sequence<Pair<T, Int>>, speed: Double, children: RBuilder.(T) -> Unit) = child(
    createElement(FrameRunner, FrameRunnerProps(sequence, speed), { value: T ->
        buildElements { children(value) }
    })
)

val FrameRunner =
    reactFunction<FrameRunnerProps> { props ->
        val (sequence, speed) = props
        val (state, setState) = useState(sequence.first().first)
        val scheduleStateFunc = scheduleStateFunc(setState, speed)

        useEffect(emptyList()) { sequence.forEach(scheduleStateFunc) }
        children(state, props)
    }

private fun scheduleStateFunc(setState: (Any?) -> Unit, speed: Double) = setState.statePairToTimeoutArgsFunc()
    .join(pairTransformSecondFunc { it.applySpeed(speed) })
    .join { args -> args.let(::setTimeout); Unit }

private fun Int.applySpeed(speed: Double): Int = round(this / speed).toInt()

private fun ((Any?) -> Unit).statePairToTimeoutArgsFunc(): (Pair<Any?, Int>) -> Pair<() -> Unit, Int> =
    pairTransformFirstFunc(curryOneArgToNoArgsFunc())


private fun <I, O, V> pairTransformFirstFunc(transform: (I) -> O): (Pair<I, V>) -> Pair<O, V> = { pair ->
    pair.letFirst(transform)
}

private fun <I, O, K> pairTransformSecondFunc(transform: (I) -> O): (Pair<K, I>) -> Pair<K, O> = { pair ->
    pair.letSecond(transform)
}

private fun setTimeout(timedFunc: () -> Unit, time: Int) = window.setTimeout(timedFunc, time)

private fun RBuilder.children(state: Any?, props: FrameRunnerProps): Any {
    val unsafeCast = props.children.unsafeCast<(Any?) -> Any>()
    val children = unsafeCast(state)
    return childList.add(children)
}
