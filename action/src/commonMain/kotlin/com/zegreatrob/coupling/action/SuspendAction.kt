package com.zegreatrob.coupling.action

interface DispatchableAction<in T, R> : Action

interface SuspendAction<in T, R> : DispatchableAction<T, R> {
    suspend fun execute(dispatcher: T): Result<R>
}

interface ExecutableAction<in T, R> : DispatchableAction<T, R> {
    fun execute(dispatcher: T): Result<R>
}

interface SuccessfulExecutableAction<in T, R> : ExecutableAction<T, R>, Action {
    override fun execute(dispatcher: T): SuccessfulResult<R>
}

interface SimpleSuccessfulExecutableAction<T, R> : SuccessfulExecutableAction<T, R> {
    override fun execute(dispatcher: T) = perform(dispatcher)
    val perform: (T) -> SuccessfulResult<R>

    fun <A> A.link(performFunc: (T, A) -> R): (T) -> SuccessfulResult<R> = { performFunc(it, this).successResult() }
}

interface SimpleSuspendAction<T, R> : SuspendAction<T, R> {
    override suspend fun execute(dispatcher: T) = perform(dispatcher)
    val perform: SuspendPerformFunc2<T, R>

    fun <A> A.link(performFunc: SuspendPerformFunc<A, T, R>): SuspendPerformFunc2<T, R> = { performFunc(it, this) }
}

typealias SuspendPerformFunc<A, D, R> = suspend (D, A) -> Result<R>
typealias SuspendPerformFunc2<D, R> = suspend (D) -> Result<R>

typealias PerformFunc<A, D, R> = (D, A) -> Result<R>
typealias PerformFunc2<D, R> = (D) -> Result<R>
