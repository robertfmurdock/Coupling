package com.zegreatrob.coupling.model.tribe

import kotlinx.coroutines.GlobalScope

interface TribeIdGetSyntax {
    val tribeRepository: TribeGet
    fun TribeId.loadAsync() = with(tribeRepository) { GlobalScope.getTribeAsync(this@loadAsync) }
}