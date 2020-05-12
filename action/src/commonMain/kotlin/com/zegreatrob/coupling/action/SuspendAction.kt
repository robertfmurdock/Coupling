package com.zegreatrob.coupling.action

interface SuspendAction<T, R> : Action {
    suspend fun execute(dispatcher: T): Result<R>
}

interface SimpleSuspendAction<T, R> : SuspendAction<T, R> {
    override suspend fun execute(dispatcher: T) = perform(dispatcher)
    val perform: PerformFunc2<T, R>

    fun <A> A.link(performFunc: PerformFunc<A, T, R>): PerformFunc2<T, R> = { performFunc(it, this) }
}

typealias PerformFunc<A, D, R> = suspend (D, A) -> Result<R>
typealias PerformFunc2<D, R> = suspend (D) -> Result<R>


sealed class Result<V>

data class SuccessfulResult<V>(val value: V) : Result<V>()

data class NotFoundResult<V>(val entityName: String) : Result<V>()

class UnauthorizedResult<V>() : Result<V>()

fun <V> V.successResult() = SuccessfulResult(this)

fun Boolean.deletionResult(entityName: String): Result<Unit> = if (this)
    SuccessfulResult(Unit)
else
    NotFoundResult(entityName)
