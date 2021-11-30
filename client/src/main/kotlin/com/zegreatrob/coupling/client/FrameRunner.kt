package com.zegreatrob.coupling.client

import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import react.*
import kotlin.math.round

data class FrameRunnerProps(
    val sequence: Sequence<Frame<*>>,
    val speed: Double,
    val children: RBuilder.(value: Any?) -> Unit
) : Props

private fun <A, B, A2> Pair<A, B>.letFirst(transform: (A) -> A2) = transform(first) to second
private fun <A, B, B2> Pair<A, B>.letSecond(transform: (B) -> B2) = first to transform(second)
private fun <A, B, C> Pair<A, B>.let(transform: (A, B) -> C) = transform(first, second)
private fun <I, O, O2> ((I) -> O).join(transform: (O) -> O2) = { pair: I -> transform(this(pair)) }
private fun <I> ((I?) -> Unit).curryOneArgToNoArgsFunc(): (I) -> () -> Unit = { it: I -> { this(it) } }

data class Frame<T>(val data: T, val delay: Int)

fun <T> RBuilder.frameRunner(sequence: Sequence<Frame<T>>, speed: Double, children: RBuilder.(T) -> Unit) = child(
    createElement(FrameRunner, FrameRunnerProps(sequence, speed) { value ->
        children(value.unsafeCast<T>())
    })
)

val FrameRunner = reactFunction<FrameRunnerProps> { props ->
    val (sequence, speed) = props
    var state by useState(sequence.first().data)
    val scheduleStateFunc: (Frame<*>) -> Unit = scheduleStateFunc({ state = it }, speed)

    useEffectOnce { sequence.forEach(scheduleStateFunc) }
    props.children(this, state)
}

private fun scheduleStateFunc(setState: (Any?) -> Unit, speed: Double) = setState.statePairToTimeoutArgsFunc()
    .join(pairTransformSecondFunc { it.applySpeed(speed) })
    .join { args -> args.let(::setTimeout); Unit }

private fun Int.applySpeed(speed: Double): Int = round(this / speed).toInt()

private fun ((Any?) -> Unit).statePairToTimeoutArgsFunc(): (Frame<*>) -> Pair<() -> Unit, Int> =
    toFrameFunc(pairTransformFirstFunc(curryOneArgToNoArgsFunc()))

fun toFrameFunc(target: (Pair<Any?, Int>) -> Pair<() -> Unit, Int>) = fun(it: Frame<*>): Pair<() -> Unit, Int> {
    return target(Pair(it.data, it.delay))
}

private fun <I, O, V> pairTransformFirstFunc(transform: (I) -> O): (Pair<I, V>) -> Pair<O, V> = { pair ->
    pair.letFirst(transform)
}

private fun <I, O, K> pairTransformSecondFunc(transform: (I) -> O): (Pair<K, I>) -> Pair<K, O> = { pair ->
    pair.letSecond(transform)
}

private fun setTimeout(timedFunc: () -> Unit, time: Int) = window.setTimeout(timedFunc, time)

