package com.zegreatrob.coupling.action

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

interface ScopeProvider {
    fun buildScope(): CoroutineScope = MainScope()

    companion object : ScopeProvider
}