package com.zegreatrob.coupling.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async

data class DataLoadComponentTools(val scope: CoroutineScope, val reloadData: () -> Unit) {

    @ExperimentalCoroutinesApi
    fun <R> performSuspendWork(
        work: suspend () -> R,
        errorResult: (Throwable) -> R,
        onWorkComplete: (R) -> Unit
    ) {
        val deferred = scope.async { work() }
        deferred.invokeOnCompletion { handler ->
            if (handler != null) {
                onWorkComplete(errorResult(handler))
            } else {
                deferred.getCompleted().let(onWorkComplete)
            }
        }
    }

}
