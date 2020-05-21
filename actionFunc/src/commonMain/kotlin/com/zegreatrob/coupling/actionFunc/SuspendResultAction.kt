package com.zegreatrob.coupling.actionFunc

typealias SuspendResultAction<T, R> = SuspendAction<T, Result<R>>

interface SimpleSuspendResultAction<T, R> : SuspendResultAction<T, R>, SimpleSuspendAction<T, Result<R>> {
    override suspend fun execute(dispatcher: T) = performFunc(dispatcher)
}
