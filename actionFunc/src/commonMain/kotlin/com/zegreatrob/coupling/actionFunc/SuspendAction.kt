package com.zegreatrob.coupling.actionFunc

interface SuspendAction<in T, R> : DispatchableAction<T, R> {
    suspend fun execute(dispatcher: T): R
}
