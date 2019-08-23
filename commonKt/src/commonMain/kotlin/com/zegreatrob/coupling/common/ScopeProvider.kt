package com.zegreatrob.coupling.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

interface ScopeProvider {
    fun buildScope(): CoroutineScope = MainScope()
}