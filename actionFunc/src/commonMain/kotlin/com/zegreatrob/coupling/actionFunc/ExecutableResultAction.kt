package com.zegreatrob.coupling.actionFunc

interface ExecutableResultAction<in T, R> : ExecutableAction<T, Result<R>> {
    override fun execute(dispatcher: T): Result<R>
}
