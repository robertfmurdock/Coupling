package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.wdio.cli.Launcher
import kotlinx.coroutines.await
import kotlin.js.json

suspend fun runWebdriverIO(configPath: String) = Launcher(configPath, json())
    .run()
    .await()