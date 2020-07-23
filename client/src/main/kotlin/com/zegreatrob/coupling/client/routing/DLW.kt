package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.external.react.useScope
import com.zegreatrob.minreact.reactFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import react.FunctionalComponent
import react.RProps
import react.RSetState
import react.useState

sealed class DataLoadState<D>
class EmptyState<D> : DataLoadState<D>()
data class PendingState<D>(val job: Job) : DataLoadState<D>()
data class ResolvedState<D>(val result: D) : DataLoadState<D>()

data class DLWProps<D>(val getDataAsync: DataloadPropsFunc<D>, val jobErrorState: (Throwable) -> D) : RProps

fun <D> dLW() = reactFunction<DLWProps<D>> { props ->
    val (getDataAsync, jobErrorState) = props
    val (state, setState) = useState<DataLoadState<D>> { EmptyState() }
    val scope = useScope("Data load")

    if (state is EmptyState) {
        startPendingJob(scope, setState, getDataAsync, jobErrorState)
    }

    props.children(state)
}.unsafeCast<FunctionalComponent<DLWProps<D>>>()

private fun <D> startPendingJob(
    scope: CoroutineScope,
    setState: RSetState<DataLoadState<D>>,
    getDataAsync: DataloadPropsFunc<D>,
    jobErrorState: (Throwable) -> D
) {
    val setEmpty = setState.empty()
    val setPending = setState.pending()
    val setResolved = setState.resolved()
    setPending(
        scope.launch { getDataAsync(setEmpty, scope).let(setResolved) }
            .also { job -> job.errorOnJobFailure(setResolved, jobErrorState) }
    )
}

private fun <D> Job.errorOnJobFailure(setResolved: (D) -> Unit, errorResult: (Throwable) -> D) =
    invokeOnCompletion { cause ->
        if (cause != null) {
            setResolved(errorResult(cause))
        }
    }

private fun <D> RSetState<DataLoadState<D>>.empty(): () -> Unit = { this(EmptyState()) }

private fun <D> RSetState<DataLoadState<D>>.pending(): (Job) -> Unit = { this(PendingState(it)) }

private fun <D> RSetState<DataLoadState<D>>.resolved(): (D) -> Unit = { this(ResolvedState(it)) }
