package com.zegreatrob.coupling.actionFunc

interface ExecutableResultAction<in T, R> : ExecutableAction<T, Result<R>> {
    override fun execute(dispatcher: T): Result<R>
}

interface SuccessfulExecutableAction<in T, R> : ExecutableResultAction<T, R>, Action {
    override fun execute(dispatcher: T): SuccessfulResult<R>
}

interface SimpleSuccessfulExecutableAction<T, R> : SuccessfulExecutableAction<T, R> {
    override fun execute(dispatcher: T) = performFunc(dispatcher)
    val performFunc: (T) -> SuccessfulResult<R>
    fun <A> A.link(performFunc: (T, A) -> R): (T) -> SuccessfulResult<R> = { performFunc(it, this).successResult() }
}
