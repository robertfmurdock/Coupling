package com.zegreatrob.coupling.actionFunc

interface SuspendAction<in T, R> : DispatchableAction<T, R> {
    suspend fun execute(dispatcher: T): R
}

interface SimpleSuspendAction<T, R> : SuspendAction<T, R> {
    override suspend fun execute(dispatcher: T) = performFunc(dispatcher)
    val performFunc: suspend (T) -> R
    fun <A> A.link(performFunc: suspend (T, A) -> R): suspend (T) -> R = { performFunc(it, this) }
}
