package com.zegreatrob.coupling.actionFunc

interface SuspendResultAction<in T, R> : SuspendAction<T, Result<R>> {
    override suspend fun execute(dispatcher: T): Result<R>
}

interface SimpleSuspendAction<T, R> : SuspendResultAction<T, R> {
    override suspend fun execute(dispatcher: T) = performFunc(dispatcher)
    val performFunc: SuspendPerformFunc2<T, R>

    fun <A> A.link(performFunc: SuspendPerformFunc<A, T, R>): SuspendPerformFunc2<T, R> = { performFunc(it, this) }
}

typealias SuspendPerformFunc<A, D, R> = suspend (D, A) -> Result<R>
typealias SuspendPerformFunc2<D, R> = suspend (D) -> Result<R>
