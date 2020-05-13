package com.zegreatrob.coupling.action

interface SuspendAction<in T, R> : Action {
    suspend fun execute(dispatcher: T): Result<R>
}

interface ExecutableAction<in T, R> : Action {
    fun execute(dispatcher: T): Result<R>
}

interface SuccessfulExecutableAction<in T, R> : ExecutableAction<T, R>, Action {
    override fun execute(dispatcher: T): SuccessfulResult<R>
}

interface SimpleExecutableAction<T, R> : ExecutableAction<T, R> {
    override fun execute(dispatcher: T) = perform(dispatcher)
    val perform: PerformFunc2<T, R>

    fun <A> A.link(performFunc: PerformFunc<A, T, R>): PerformFunc2<T, R> = { performFunc(it, this) }
}

interface SimpleSuccessfulExecutableAction<T, R> : SuccessfulExecutableAction<T, R> {
    override fun execute(dispatcher: T) = perform(dispatcher)
    val perform: (T) -> SuccessfulResult<R>

    fun <A> A.link(performFunc: (T, A) -> SuccessfulResult<R>): (T) -> SuccessfulResult<R> = { performFunc(it, this) }
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
