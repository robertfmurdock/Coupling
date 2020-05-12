package com.zegreatrob.coupling.action

sealed class Result<V>
data class SuccessfulResult<V>(val value: V) : Result<V>()
data class NotFoundResult<V>(val entityName: String) : Result<V>()
class UnauthorizedResult<V> : Result<V>()

fun <V> V.successResult() = SuccessfulResult(this)

fun Boolean.deletionResult(entityName: String): Result<Unit> = if (this)
    SuccessfulResult(Unit)
else
    NotFoundResult(entityName)
