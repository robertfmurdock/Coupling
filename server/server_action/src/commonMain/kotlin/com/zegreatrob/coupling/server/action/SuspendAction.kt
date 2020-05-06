package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.Action

interface SuspendAction<T, R> : Action {
    suspend fun execute(dispatcher: T): R
}