package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.Action

interface SuspendAction<T, R> : Action {
    suspend fun execute(dispatcher: T): Result<R>
}

sealed class Result<V>

data class SuccessfulResult<V>(val value: V) : Result<V>()

data class NotFoundResult<V>(val entityName: String) : Result<V>()

class UnauthorizedResult<V>() : Result<V>()

fun <V> V.successResult() = SuccessfulResult(this)

fun Boolean.deletionResult(entityName: String): Result<Unit> = if (this)
    SuccessfulResult(Unit)
else
    NotFoundResult(entityName)
