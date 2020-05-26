package com.zegreatrob.coupling.actionFunc

interface ExecutableAction<in T, R> : DispatchableAction<T, R> {
    fun execute(dispatcher: T): R
}
