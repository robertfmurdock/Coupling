package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.wdio.Launcher
import kotlinx.coroutines.await
import kotlin.js.json

suspend fun runWebdriverIO(configPath: String) = Launcher(
    configPath,
    json()
).run()
    .await()