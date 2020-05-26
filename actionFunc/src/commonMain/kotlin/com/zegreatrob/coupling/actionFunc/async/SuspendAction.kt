package com.zegreatrob.coupling.actionFunc.async

import com.zegreatrob.coupling.actionFunc.DispatchableAction

interface SuspendAction<in T, R> : DispatchableAction<T, R> {
    suspend fun execute(dispatcher: T): R
}
