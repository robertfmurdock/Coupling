package com.zegreatrob.coupling.action

sealed class Result<V>
data class SuccessfulResult<V>(val value: V) : Result<V>()
data class NotFoundResult<V>(val entityName: String) : Result<V>()
data class ErrorResult<V>(val message: String) : Result<V>()
class UnauthorizedResult<V> : Result<V>()

fun <V> V.successResult() = SuccessfulResult(this)

fun Boolean.deletionResult(entityName: String): Result<Unit> = if (this) {
    SuccessfulResult(Unit)
} else {
    NotFoundResult(entityName)
}

fun <V> Result<V>.valueOrNull() = when (this) {
    is SuccessfulResult -> value
    else -> null
}

inline fun <V1, V2> Result<V1>.transform(transform: (V1) -> V2): Result<V2> = when (this) {
    is SuccessfulResult -> transform(value).successResult()
    is NotFoundResult -> NotFoundResult(
        entityName,
    )

    is UnauthorizedResult -> UnauthorizedResult()
    is ErrorResult -> ErrorResult(
        message,
    )
}
