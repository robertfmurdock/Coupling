package com.zegreatrob.coupling.actionFunc

interface SuspendAction<in T, R> : DispatchableAction<T, R> {
    suspend fun execute(dispatcher: T): Result<R>
}

interface SimpleSuspendAction<T, R> : SuspendAction<T, R> {
    override suspend fun execute(dispatcher: T) = performFunc(dispatcher)
    val performFunc: SuspendPerformFunc2<T, R>

    fun <A> A.link(performFunc: SuspendPerformFunc<A, T, R>): SuspendPerformFunc2<T, R> = { performFunc(it, this) }
}

typealias SuspendPerformFunc<A, D, R> = suspend (D, A) -> Result<R>
typealias SuspendPerformFunc2<D, R> = suspend (D) -> Result<R>
