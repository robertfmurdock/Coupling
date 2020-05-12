package com.zegreatrob.coupling.action

interface SuspendAction<in T, R> : Action {
    suspend fun execute(dispatcher: T): Result<R>
}

interface SimpleSuspendAction<T, R> : SuspendAction<T, R> {
    override suspend fun execute(dispatcher: T) = perform(dispatcher)
    val perform: PerformFunc2<T, R>

    fun <A> A.link(performFunc: PerformFunc<A, T, R>): PerformFunc2<T, R> = { performFunc(it, this) }
}

typealias PerformFunc<A, D, R> = suspend (D, A) -> Result<R>
typealias PerformFunc2<D, R> = suspend (D) -> Result<R>
