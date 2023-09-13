package com.zegreatrob.coupling.cli

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.joinAll

class CliScope : CoroutineScope {
    override val coroutineContext = SupervisorJob()

    suspend fun joinAll() {
        val job = coroutineContext[Job]
        job?.children?.toList()?.joinAll()
    }
}
